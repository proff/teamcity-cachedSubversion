package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.CACHE_PATH_CONFIG_KEY
import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.DISABLED_CONFIG_KEY
import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import com.proff.teamcity.cachedSubversion.svnClient.iSvnClientFactory
import com.proff.teamcity.cachedSubversion.svnClient.svnClient
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.messages.DefaultMessagesInfo
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.Closeable
import java.io.File
import java.util.concurrent.CancellationException

class cachedSubversionCore(val build: iRunningBuild, val svnClientFactory: iSvnClientFactory, val checkoutSettings: checkoutSettings, val cacher: iCacher) {
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

            build.activity(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT).use {
                for (vcs in checkoutSettings.vcs) {
                    val client = svnClientFactory.create(vcs.login, vcs.password)
                    var svnUrl = SVNURL.parseURIEncoded(vcs.url)
                    if (!beforeSwabra) {
                        val newUrl = cacher.doCache(vcs, client)
                        if (newUrl != null)
                            svnUrl = newUrl
                    }
                    if (build.interruptReason() != null)
                        return

                    val activity = if (beforeSwabra) Closeable { } else build.activity("checkout from $svnUrl", DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
                    activity.use {
                        for (rule in vcs.rules) {
                            if (rule.exclude) {
                                build.warning("exclude rule is ignored: $rule")
                                continue
                            }
                            build.message(rule.toString())
                            var fromUrl = svnUrl
                            if (!rule.from.isNullOrBlank())
                                fromUrl = fromUrl.appendPath(rule.from, false)
                            var toPath = build.checkoutDirectory()
                            if (!rule.to.isNullOrBlank())
                                toPath = File(toPath, rule.to)
                            if (beforeSwabra) {
                                if (checkoutSettings.revert)
                                    client.revert(toPath)
                                if (checkoutSettings.clean)
                                    client.cleanup(toPath)
                            } else
                                client.checkout(fromUrl, toPath, SVNRevision.create(vcs.revision), checkoutSettings)
                        }
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