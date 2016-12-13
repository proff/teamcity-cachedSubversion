package com.proff.teamcity.cachedSubversion
import com.proff.teamcity.cachedSubversion.svnClient.iSvnClientFactory
import jetbrains.buildServer.agentServer.AgentBuild
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNURL
import java.io.Closeable

class cachedSubversionCore(val build: iRunningBuild, val svnClientFactory: iSvnClientFactory, val checkoutSettings: checkoutSettings, val cacheHelper: iCacheHelper, val checkoutHelper: iCheckoutHelper) {
    fun run(beforeSwabra: Boolean) {
        val activityName = if (beforeSwabra) "cachedSubversion revert" else "cachedSubversion"
        try {
            if (checkoutSettings.enabled && build.checkoutType() != AgentBuild.CheckoutType.MANUAL) {
                build.warning("Subversion caching disabled because manual checkout mode required")
                return
            }
            if (!checkoutSettings.enabled)
                return
            if ((checkoutSettings.mode != checkoutMode.Checkout || !(checkoutSettings.revert || checkoutSettings.clean)) && beforeSwabra)
                return

            if (checkoutSettings.containsOtherVcs)
                build.warning("configuraion contains not subversion vcs, you should checkout it manually")
            build.activity(activityName).use {
                for (vcs in checkoutSettings.vcs) {
                    val client = svnClientFactory.create(vcs.login, vcs.password)
                    var svnUrl = SVNURL.parseURIEncoded(vcs.url)
                    if (!beforeSwabra) {
                        val newUrl = cacheHelper.doCache(vcs, client)
                        if (newUrl != null)
                            svnUrl = newUrl
                    }
                    if (build.interruptReason() != null)
                        return

                    val activity = if (beforeSwabra) Closeable { } else build.activity("checkout from $svnUrl")
                    activity.use {
                        checkoutHelper.doCheckout(svnUrl, checkoutSettings, vcs, client, beforeSwabra)
                    }
                }
                build.message("completed")
            }
        } catch(e: SVNCancelException) {
            build.warning("cancelled")
        } catch(e: Exception) {
            build.stopBuild("error in cachedSubersion: $e")
            throw e
        }
    }
}