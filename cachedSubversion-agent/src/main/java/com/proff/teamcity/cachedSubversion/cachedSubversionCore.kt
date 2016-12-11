package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.CACHE_PATH_CONFIG_KEY
import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.DISABLED_CONFIG_KEY
import com.proff.teamcity.cachedSubversion.svnClient.svnClient
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.messages.DefaultMessagesInfo
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
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
        val settings = getCheckoutSettings(caching.single().parameters)
        if ((settings.mode != checkoutMode.Checkout || !settings.revert) && beforeSwabra)
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
                val cacheRule = getCacheUrl(url, runningBuild)
                if (cacheRule != null) {
                    val target = doCache(cacheRule, runningBuild, revision, client)
                    val root = client.getRootUri(url, SVNRevision.create(revision))
                    svnUrl = target.appendPath(url.removePrefix(root.toString()), false)
                }
            }
            if (runningBuild.interruptReason != null)
                return

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
                    client.checkout(fromUrl, toPath, SVNRevision.create(revision), settings)
            }
        }
        logger.message("completed")
    } catch(e: SVNCancelException) {
        logger.warning("cancelled")
    } catch(e: Exception) {
        runningBuild.stopBuild("error in cachedSubersion: $e")
        throw e
    } finally {
        logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    }
}

private fun doCache(rule: cacheRule, runningBuild: AgentRunningBuild, revision: Long, client: svnClient): SVNURL {
    val logger = runningBuild.buildLogger
    val activityName = "caching ${rule.source}"
    logger.activityStarted(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    try {
        val cacheTarget = getCacheTarget(runningBuild, rule)
        if (cacheTarget.file != null && !cacheTarget.file.exists()) {
            cacheTarget.file.mkdirs()
            logger.message("creating cache repository")
            client.createAndInitialize(rule.source, cacheTarget.file)
        } else {
            client.initializeIfRequired(rule.source, cacheTarget.url)
        }
        logger.message("synchronizing to ${cacheTarget.url}")
        doSync(cacheTarget, revision, client, runningBuild)
        return cacheTarget.url
    } finally {
        logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_CHECKOUT)
    }
}

private fun doSync(cacheTarget: cacheTarget, revision: Long, client: svnClient, runningBuild: AgentRunningBuild) {
    val logger = runningBuild.buildLogger
    var delay = 1
    while (true) {
        try {
            if (runningBuild.interruptReason != null) {
                return
            }
            val lastRevision = client.lastRevision(cacheTarget.url)
            if (lastRevision < revision) {
                client.synchronize(cacheTarget.url)
                if (cacheTarget.file != null) {
                    val newLastRevision = client.lastRevision(cacheTarget.url)
                    if (lastRevision / 1000 < newLastRevision / 1000) {
                        logger.message("packing")
                        client.pack(cacheTarget.file)
                    }
                }
                return
            }
        } catch(e: RepositoryLockedException) {
            logger.message("repo is locked by ${e.holder}, retry after $delay seconds")
            Thread.sleep((delay * 1000).toLong())
            if (delay < 60)
                delay++
        }
    }
}

private fun getCacheTarget(runningBuild: AgentRunningBuild, cacheRule: cacheRule): cacheTarget {
    if (!cacheRule.name.isNullOrBlank()) {
        val customPath = runningBuild.agentConfiguration.configurationParameters[CACHE_PATH_CONFIG_KEY + "." + cacheRule.name]
        if (!customPath.isNullOrBlank()) {
            try {
                return cacheTarget(SVNURL.parseURIEncoded(customPath))
            } catch(e: SVNException) {
                return cacheTarget(File(customPath))
            }
        }
    }
    if (cacheRule.target != null)
        return cacheRule.target
    var root = File(runningBuild.agentConfiguration.systemDirectory, "cachedSubversion")
    val customPath = runningBuild.agentConfiguration.configurationParameters[CACHE_PATH_CONFIG_KEY]
    if (customPath != null)
        root = File(customPath)
    val cacheFile = File(root, md5(cacheRule.source.toString()).toHexString())
    return cacheTarget(cacheFile)
}

private fun getCacheUrl(url: String, runningBuild: AgentRunningBuild): cacheRule? {
    val params = runningBuild.agentConfiguration.configurationParameters
    if (params.containsKey(DISABLED_CONFIG_KEY))
        return null
    val rules = getCacheRules(runningBuild)
    for (rule in rules) {
        val cur = rule.source.removePathTail().toString()
        if (!cur.isNullOrBlank() && (url == cur || url.startsWith(cur + "/")))
            return rule
    }
    return null
}

private fun getCheckoutSettings(parameters: Map<String, String>): checkoutSettings {
    var mode = checkoutMode.Checkout
    var revert = false
    var clean = false
    when (parameters[cachedSubversionConstants.MODE_CONFIG_KEY]) {
        "revertCheckout" -> {// obsolete value
            mode = checkoutMode.Checkout
            revert = true
            clean = true
        }
        "checkout" -> {// obsolete value
            mode = checkoutMode.Checkout
            revert = false
            clean = false
        }
        "deleteExport" -> {//obsolete value
            mode = checkoutMode.Export
            clean = true
        }
        "export" -> {// obsolete value
            mode = checkoutMode.Export
            clean = false
        }
        "Checkout" -> {
            mode = checkoutMode.Checkout
        }
        "Export" -> {
            mode = checkoutMode.Export
        }
    }
    if (mode == checkoutMode.Checkout) {
        if (parameters.containsKey(cachedSubversionConstants.REVERT_CONFIG_KEY))
            revert = true
        if (parameters.containsKey(cachedSubversionConstants.CLEAN_CONFIG_KEY))
            clean = true
    } else {
        if (parameters.containsKey(cachedSubversionConstants.DELETE_CONFIG_KEY))
            clean = true
    }
    return checkoutSettings(mode, revert, clean)
}

private fun getCacheRules(runningBuild: AgentRunningBuild): List<cacheRule> {
    val value = runningBuild.sharedConfigParameters[cachedSubversionConstants.REPOSITORIES_CONFIG_KEY]
    var result = listOf<cacheRule>()
    if (value != null)
        result = value.split("\n").map { cacheRule(it.trim()) }
    return result
}