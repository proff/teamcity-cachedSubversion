package com.proff.teamcity.cachedSubversion.svnClient

import com.proff.teamcity.cachedSubversion.RepositoryLockedException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

interface iSvnClient {
    fun getRootUri(url: String, revision: SVNRevision): SVNURL
    fun createAndInitialize(from: SVNURL, to: File)
    fun initializeIfRequired(from: SVNURL, to: SVNURL)
    fun lastRevision(url: SVNURL): Long

    @Throws(RepositoryLockedException::class)
    fun synchronize(url: SVNURL)

    fun pack(file: File)
}