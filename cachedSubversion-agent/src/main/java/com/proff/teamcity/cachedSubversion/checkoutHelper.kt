package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

class checkoutHelper(val build: iRunningBuild) : iCheckoutHelper {
    override fun doCheckout(url: SVNURL, settings: checkoutSettings, vcsSettings: vcsCheckoutSettings, client: iSvnClient, beforeSwabra: Boolean) {
        for (rule in vcsSettings.rules) {
            if (rule.exclude) {
                build.warning("exclude rule is ignored: $rule")
                continue
            }
            build.message(rule.toString())
            var fromUrl = url
            if (!rule.from.isNullOrBlank())
                fromUrl = fromUrl.appendPath(rule.from, false)
            var toPath = build.checkoutDirectory()
            if (!rule.to.isNullOrBlank())
                toPath = File(toPath, rule.to)
            if (beforeSwabra) {
                if (settings.mode == checkoutMode.Checkout) {
                    if (settings.clean)
                        client.cleanup(toPath)
                    if (settings.revert)
                        client.revert(toPath)
                }
            } else
                client.checkout(fromUrl, toPath, SVNRevision.create(vcsSettings.revision), settings)
        }
    }
}