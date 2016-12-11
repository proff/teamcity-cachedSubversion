package com.proff.teamcity.cachedSubversion.svnClient

import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNStatus
import org.tmatesoft.svn.core.wc.admin.SVNSyncInfo
import java.io.File

interface iSvnKit {
    fun doCreateRepository(path: File, uuid: String?, enableRevisionProperties: Boolean, force: Boolean): SVNURL
    fun doInitialize(fromURL: SVNURL, toURL: SVNURL)
    fun getRevisionProperties(url: SVNURL, revision: Long, properties: Nothing?): SVNProperties

    @Throws(SVNException::class)
    fun doSynchronize(toURL: SVNURL, function: (SVNLogEntry) -> Unit)

    fun doPack(repositoryRoot: File)
    fun doInfo(url: SVNURL): SVNSyncInfo
    fun doExport(url: SVNURL, dstPath: File, pegRevision: SVNRevision, revision: SVNRevision, eolStyle: String?, overwrite: Boolean, depth: SVNDepth): Long
    fun doStatus(path: File, remote: Boolean): SVNStatus
    fun doUpdate(path: File, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean, depthIsSticky: Boolean): Long
    fun doCheckout(url: SVNURL, dstPath: File, pegRevision: SVNRevision?, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean): Long
    fun getReposRoot(path: File?, url: SVNURL?, pegRevision: SVNRevision): SVNURL
    fun doRevert(paths: Array<File>, depth: SVNDepth, changeLists: MutableList<String>)
    fun doCleanup(path: File)
    fun checkPath(url: SVNURL, path: String, revision: SVNRevision): SVNNodeKind
}