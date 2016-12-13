package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import org.tmatesoft.svn.core.SVNURL

interface iCheckoutHelper {
    fun doCheckout(url: SVNURL, settings: checkoutSettings, vcsSettings: vcsCheckoutSettings, client: iSvnClient, beforeSwabra: Boolean)
}