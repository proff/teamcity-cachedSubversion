package com.proff.teamcity.cachedSubversion.svnClient

import com.proff.teamcity.cachedSubversion.*
import jetbrains.buildServer.agent.AgentRunningBuild
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.internal.util.SVNUUIDGenerator
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

open class svnClient(val kit: iSvnKit, val build: iRunningBuild, val fileHelper: iFileHelper) : iSvnClient {
    companion object {
        fun create(user: String?, password: String?, build: AgentRunningBuild): svnClient {
            val authManager = DefaultSVNAuthenticationManager(null, false, user, password?.toCharArray(), null, null)
            if (user != null)
                authManager.isAuthenticationForced = true
            return svnClient(svnKit(authManager, { if (build.interruptReason != null) throw SVNCancelException() }), runningBuild(build), fileHelper())
        }
    }

    override fun createAndInitialize(from: SVNURL, to: File) {
        kit.doCreateRepository(to, SVNUUIDGenerator.generateUUIDString(), true, false)
        kit.doInitialize(from, SVNURL.fromFile(to))
    }

    override fun initializeIfRequired(from: SVNURL, to: SVNURL) {
        val properties = kit.getRevisionProperties(to, 0, null)
        if (properties.getStringValue("svn:sync-from-uuid") == null)
            kit.doInitialize(from, to)
    }

    override fun synchronize(url: SVNURL) {
        try {
            val locked = getSyncLockedBy(url)
            if (locked != null)
                throw RepositoryLockedException(locked)
            kit.doSynchronize(url) {
                build.message(it.revision.toString())
            }
        } catch(e: SVNException) {
            if (e.errorMessage?.errorCode?.code == 204899 && e.errorMessage?.childErrorMessage != null) {
                val child = e.errorMessage.childErrorMessage
                val code = child.errorCode?.code
                if (code != 204899)
                    throw e
                val message = child.message
                val match = Regex("Failed to get lock on destination repos, currently held by '(.*)'").find(message) ?: throw e
                throw RepositoryLockedException(match.groupValues[1])
            }
            throw e
        }
    }

    override fun pack(file: File) {
        kit.doPack(file)
    }

    override fun lastRevision(url: SVNURL): Long {
        return kit.doInfo(url).lastMergedRevision
    }

    fun checkout(url: SVNURL, to: File, revision: SVNRevision, settings: checkoutSettings) {
        if (settings.mode == checkoutMode.Export) {
            if (settings.clean) {
                if (!fileHelper.deleteRecursively(to))
                    build.warning("Directory $to can't be completely deleted")
            }
            kit.doExport(url, to, revision, revision, null, true, SVNDepth.INFINITY)
            return
        }
        if (fileHelper.isDirectory(to) && fileHelper.isDirectory(File(to, ".svn"))) {
            val root = kit.doStatus(to, false).remoteURL
            if (root == url) {
                kit.doUpdate(to, revision, SVNDepth.INFINITY, true, true)
                return
            }
            if (!fileHelper.deleteRecursively(to)) {
                throw cachedSubversionException("could not delete folder $to")
            }
        }
        if (isFile(url, revision)) {
            kit.doExport(url, to, revision, revision, null, true, SVNDepth.INFINITY)
        } else {
            kit.doCheckout(url, to, revision, revision, SVNDepth.INFINITY, true)
        }
    }

    override fun getRootUri(url: String, revision: SVNRevision): SVNURL {
        return getRootUri(SVNURL.parseURIEncoded(url), revision)
    }

    fun getRootUri(url: SVNURL, revision: SVNRevision): SVNURL {
        return kit.getReposRoot(null, url, revision)
    }

    fun revert(to: File) {
        if (fileHelper.isDirectory(to) && fileHelper.isDirectory(File(to, ".svn"))) {
            val list = mutableListOf<String>()
            kit.doRevert(arrayOf(to), SVNDepth.INFINITY, list)
        }
    }

    fun cleanup(to: File) {
        if (fileHelper.isDirectory(to) && fileHelper.isDirectory(File(to, ".svn"))) {
            kit.doCleanup(to)
        }
    }

    fun isFile(url: SVNURL, revision: SVNRevision): Boolean {
        val root = getRootUri(url, revision)
        val path = url.toString().removePrefix(root.toString())
        val nodeKind = kit.checkPath(root, path, revision)
        return nodeKind == SVNNodeKind.FILE
    }

    private fun getSyncLockedBy(url: SVNURL): String? {
        var properties = kit.getRevisionProperties(url, 0, null)
        return properties.getStringValue("svn:sync-lock")
    }

}