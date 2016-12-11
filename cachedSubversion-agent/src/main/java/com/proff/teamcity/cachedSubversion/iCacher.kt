package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import org.tmatesoft.svn.core.SVNURL

interface iCacher {
    fun doCache(vcs: vcsCheckoutSettings, client: iSvnClient): SVNURL?
}