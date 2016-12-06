package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.CACHE_PATH_CONFIG_KEY
import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.DISABLED_CONFIG_KEY
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.messages.DefaultMessagesInfo
import org.tmatesoft.svn.core.SVNURL
import java.io.File
import java.util.concurrent.CancellationException

fun run(runningBuild: AgentRunningBuild, beforeSwabra: Boolean) {
    val activityName = if (beforeSwabra) "cachedSubversion revert" else "cachedSubversion"
    val logger = runningBuild.buildLogger
    try {
        val caching = runningBuild.getBuildFeaturesOfType("cachedSubversion")
        val cachingEnabled = caching.any()
        if (cachingEnabled && runningBuild.checkoutType != AgentBuild.CheckoutType.MANUAL) {
            logger.warning("Subversion caching disabled because manual checkout mode required")
            return
        }
        if (!cachingEnabled)
            return
        val mode = getCheckoutMode(caching.single().parameters[cachedSubversionConstants.MODE_CONFIG_KEY])
        if (mode !== checkoutMode.RevertCheckout && beforeSwabra)
            return
        logger.activityStarted(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
        for (entry in runningBuild.vcsRootEntries) {
            val type = entry.vcsRoot.vcsName
            if (type != "svn")
                continue
            val url = entry.properties["url"]
            if (url == null) {
                logger.warning("url is null, skipping")
                continue
            }
            val revision = runningBuild.getBuildCurrentVersion(entry.vcsRoot).split("_")[0].toLong()
            val user = entry.properties["user"]
            val password = entry.properties["secure:svn-password"]

            val client = svnClient.create(user, password, runningBuild)
            var svnUrl = SVNURL.parseURIEncoded(url)
            if (!beforeSwabra) {
                val cacheUrl = getCacheUrl(url, runningBuild)
                if (cacheUrl != null) {
                    val target = doCache(cacheUrl, runningBuild, revision, client)
                    val root = client.getRootUri(url)
                    svnUrl = SVNURL.fromFile(File(target.absolutePath + url.removePrefix(root.toString())))
                }
            }
            var rules = entry.checkoutRulesSpecification
            if (rules.isNullOrBlank())
                rules = ".=>."
            for (rule in rules.split("\n")) {
                var r = rule.trim()
                if (r.startsWith("-:")) {
                    logger.warning("exclude rule ignored: $r")
                    continue
                }
                r = r.removePrefix("+:").trim()
                val parts = r.split("=>")
                var from = parts[0].trim()
                var to = if (parts.count() > 1) parts[1].trim() else from
                from = from.removePrefix(".").removePrefix("/").removePrefix("\\")
                to = to.removePrefix(".").removePrefix("/").removePrefix("\\")
                var fromUrl = svnUrl
                if (!from.isNullOrBlank())
                    fromUrl = fromUrl.appendPath(from, false)
                var toPath = runningBuild.checkoutDirectory
                if (!to.isNullOrBlank())
                    toPath = File(toPath, to)
                if (beforeSwabra)
                    client.revert(toPath)
                else
                    client.checkout(fromUrl, toPath, revision, mode)
            }
        }
        logger.message("completed")
    } catch(e: CancellationException) {
        logger.warning("build cancelled")
    } catch(e: Exception) {
        logger.error("error: $e")
        throw e
    } finally {
        logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    }
}

private fun doCache(cacheUrl: String, runningBuild: AgentRunningBuild, revision: Long, client: svnClient): File {
    val logger = runningBuild.buildLogger
    val activityName = "caching $cacheUrl"
    logger.activityStarted(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    try {
        val cacheTarget = getCacheTarget(runningBuild, cacheUrl)
        if (!cacheTarget.exists()) {
            cacheTarget.mkdirs()
            logger.message("creating cache repository")
            client.initialize(cacheUrl, cacheTarget)
        }
        val lastRevision = client.lastRevision(cacheTarget)
        if (lastRevision < revision) {
            logger.message("synchronizing to $cacheTarget")
            client.synchronize(cacheTarget)
            logger.message("packing")
            client.pack(cacheTarget)
        }
        return cacheTarget
    } finally {
        logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    }
}

private fun getCacheTarget(runningBuild: AgentRunningBuild, cacheUrl: String): File {
    var root = File(runningBuild.agentConfiguration.systemDirectory, "cachedSubversion")
    val customPath = runningBuild.agentConfiguration.configurationParameters[CACHE_PATH_CONFIG_KEY]
    if (customPath != null)
        root = File(customPath)
    val cacheFile = File(root, md5(cacheUrl).toHexString())
    return cacheFile
}

private fun getCacheUrl(url: String, runningBuild: AgentRunningBuild): String? {
    val params = runningBuild.agentConfiguration.configurationParameters
    if (params.containsKey(DISABLED_CONFIG_KEY))
        return null
    val repositories = runningBuild.sharedConfigParameters[cachedSubversionConstants.REPOSITORIES_CONFIG_KEY] ?: return null
    val repo = repositories.split("\n")
            .map { it.trim().trimEnd('/') }
            .firstOrNull { !it.isNullOrBlank() && (url == it || url.startsWith(it + "/")) }
    return repo
}

private fun getCheckoutMode(value: String?): checkoutMode {
    when (value) {
        "revertCheckout" -> return checkoutMode.RevertCheckout
        "checkout" -> return checkoutMode.Checkout
        "deleteExport" -> return checkoutMode.DeleteExport
        "export" -> return checkoutMode.Export
    }
    return checkoutMode.RevertCheckout
}