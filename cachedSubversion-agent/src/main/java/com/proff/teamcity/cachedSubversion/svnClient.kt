package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.auth.*
import org.tmatesoft.svn.core.internal.util.SVNUUIDGenerator
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNWCUtil
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient
import java.io.File
import java.util.concurrent.CancellationException
import javax.net.ssl.TrustManager

open class svnClient(user: String?, password: String?, val build: AgentRunningBuild) {
    companion object {
        fun create(user: String?, password: String?, build: AgentRunningBuild): svnClient {
            return svnClient(user, password, build)
        }
    }

    private val client: SVNClientManager
    private val admin: SVNAdminClient
    private val logger: BuildProgressLogger
    private val authManager: DefaultSVNAuthenticationManager

    init {
        val options = DefaultSVNOptions()
        authManager = DefaultSVNAuthenticationManager(null, false, user, password?.toCharArray(), null, null)
        if (user != null)
            authManager.isAuthenticationForced = true
        client = SVNClientManager.newInstance(options, user, password)
        client.setAuthenticationManager(authManager)
        admin = client.adminClient
        logger = build.buildLogger
    }

    fun initialize(from: String, to: File) {
        admin.doCreateRepository(to, SVNUUIDGenerator.generateUUIDString(), true, false)
        admin.doInitialize(SVNURL.parseURIEncoded(from), SVNURL.fromFile(to))
    }

    open fun synchronize(file: File) {
        try {
            val locked = getSyncLockedBy(file)
            if (locked != null)
                throw RepositoryLockedException(locked)
            admin.setReplayHandler({
                logger.message(it.revision.toString())
                if (build.interruptReason != null) {
                    throw CancellationException()
                }
            })
            admin.doSynchronize(SVNURL.fromFile(file))
        } catch(e: SVNException) {
            if (e.errorMessage?.errorCode?.code == 204899 && e?.errorMessage?.childErrorMessage != null) {
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

    fun pack(file: File) {
        admin.doPack(file)
    }

    fun lastRevision(file: File): Long {
        return client.adminClient.doInfo(SVNURL.fromFile(file)).lastMergedRevision
    }

    fun checkout(url: SVNURL, to: File, revision: Long, mode: checkoutMode) {
        if (mode == checkoutMode.DeleteExport || mode == checkoutMode.Export) {
            if (mode == checkoutMode.DeleteExport) {
                if (!to.deleteRecursively())
                    logger.warning("Directory $to can't be completely deleted")
            }
            client.updateClient.doExport(url, to, SVNRevision.create(revision), SVNRevision.create(revision), null, true, SVNDepth.INFINITY)
            return
        }
        if (to.isDirectory && File(to, ".svn").isDirectory) {
            try {
                val root = client.statusClient.doStatus(to, false).remoteURL
                if (root == url) {
                    client.updateClient.doUpdate(to, SVNRevision.create(revision), SVNDepth.INFINITY, true, true)
                    return
                }
            } catch(e: SVNException) {
            }
            if (!to.deleteRecursively()) {
                throw cachedSubversionException("could not delete folder $to")
            }
        }
        if (isFile(url)) {
            client.updateClient.doExport(url, to, SVNRevision.create(revision), SVNRevision.create(revision), null, true, SVNDepth.INFINITY)
        } else {
            client.updateClient.doCheckout(url, to, SVNRevision.create(revision), SVNRevision.create(revision), SVNDepth.INFINITY, true)
        }
    }

    fun getRootUri(url: String): SVNURL {
        return getRootUri(SVNURL.parseURIEncoded(url))
    }

    fun getRootUri(url: SVNURL): SVNURL {
        return client.logClient.getReposRoot(null, url, SVNRevision.HEAD)
    }

    fun revert(to: File) {
        if (to.isDirectory && File(to, ".svn").isDirectory) {
            val list = mutableListOf<String>()
            client.wcClient.doRevert(arrayOf(to), SVNDepth.INFINITY, list)
            if (list.count() > 0)
                logger.message("reverted files:\n" + list.joinToString())
        }
    }

    fun isFile(url: SVNURL): Boolean {
        val root = getRootUri(url)
        val repo = SVNRepositoryFactory.create(url)
        repo.authenticationManager = authManager
        val path = url.toString().removePrefix(root.toString())
        val nodeKind = repo.checkPath(path, repo.latestRevision)
        return nodeKind == SVNNodeKind.FILE
    }

    private fun getSyncLockedBy(file: File): String? {
        val repo = SVNRepositoryFactory.create(SVNURL.fromFile(file))
        repo.authenticationManager = authManager
        var properties = repo.getRevisionProperties(0, null)
        return properties.getStringValue("svn:sync-lock")
    }

}