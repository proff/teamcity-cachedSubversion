package com.proff.teamcity.cachedSubversion.svnClient

import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*
import org.tmatesoft.svn.core.wc.admin.SVNSyncInfo
import java.io.File

open class svnKit(val authManager: ISVNAuthenticationManager, val canceller: () -> Unit) : iSvnKit {
    override fun checkPath(url: SVNURL, path: String, revision: SVNRevision): SVNNodeKind {
        return createRepo(url).checkPath(path, revision.number)
    }

    override fun doRevert(paths: Array<File>, depth: SVNDepth, changeLists: MutableList<String>) {
        return createClient().wcClient.doRevert(paths, depth, changeLists)
    }

    override fun doCleanup(path: File) {
        return createClient().wcClient.doCleanup(path, true, true, true, true, true, true)
    }

    override fun getReposRoot(path: File?, url: SVNURL?, pegRevision: SVNRevision): SVNURL {
        return createClient().updateClient.getReposRoot(path, url, pegRevision)
    }

    override fun doCheckout(url: SVNURL, dstPath: File, pegRevision: SVNRevision?, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean): Long {
        return createClient().updateClient.doCheckout(url, dstPath, pegRevision, revision, depth, allowUnversionedObstructions)
    }

    override fun doUpdate(path: File, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean, depthIsSticky: Boolean): Long {
        return createClient().updateClient.doUpdate(path, revision, depth, allowUnversionedObstructions, depthIsSticky)
    }

    override fun doStatus(path: File, remote: Boolean): SVNStatus {
        return createClient().statusClient.doStatus(path, remote)
    }

    override fun doExport(url: SVNURL, dstPath: File, pegRevision: SVNRevision, revision: SVNRevision, eolStyle: String?, overwrite: Boolean, depth: SVNDepth): Long {
        return createClient().updateClient.doExport(url, dstPath, pegRevision, revision, eolStyle, overwrite, depth)
    }

    override fun doInfo(url: SVNURL): SVNSyncInfo {
        return createClient().adminClient.doInfo(url)
    }

    override fun doPack(repositoryRoot: File) {
        createClient().adminClient.doPack(repositoryRoot)
    }

    override fun doSynchronize(toURL: SVNURL, function: (SVNLogEntry) -> Unit) {
        val admin = createClient().adminClient
        admin.setReplayHandler(function)
        admin.checkCancelled()
        admin.doSynchronize(toURL)
    }

    override fun doCreateRepository(path: File, uuid: String?, enableRevisionProperties: Boolean, force: Boolean): SVNURL {
        return createClient().adminClient.doCreateRepository(path, uuid, enableRevisionProperties, force)
    }

    override fun doInitialize(fromURL: SVNURL, toURL: SVNURL) {
        createClient().adminClient.doInitialize(fromURL, toURL)
    }

    override fun getRevisionProperties(url: SVNURL, revision: Long, properties: Nothing?): SVNProperties {
        return createRepo(url).getRevisionProperties(revision, properties)
    }

    private fun createClient(): SVNClientManager {
        val client = SVNClientManager.newInstance()
        client.setAuthenticationManager(authManager)
        client.setEventHandler(cancellerClass(canceller))
        return client
    }

    private fun createRepo(url: SVNURL): SVNRepository {
        val repo = SVNRepositoryFactory.create(url)
        repo.authenticationManager = authManager
        return repo
    }

    private class cancellerClass(val canceller: () -> Unit) : SVNEventAdapter() {
        override fun checkCancelled() {
            canceller()
        }
    }
}

