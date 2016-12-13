package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import jetbrains.buildServer.messages.DefaultMessagesInfo
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

class cacheHelper(val build: iRunningBuild, val fileHelper: iFileHelper) : iCacheHelper {
    override fun doCache(vcs: vcsCheckoutSettings, client: iSvnClient): SVNURL? {
        val cacheRule = getCacheUrl(vcs.url)
        if (cacheRule != null) {
            val target = doCache(cacheRule, vcs.revision, client)
            val root = client.getRootUri(vcs.url, SVNRevision.create(vcs.revision))
            return target.appendPath(vcs.url.removePrefix(root.toString()), false)
        }
        return null
    }

    private fun getCacheUrl(url: String): cacheRule? {
        if (build.agentConfiguration(cachedSubversionConstants.DISABLED_CONFIG_KEY) != null)
            return null
        val rules = getCacheRules()
        for (rule in rules) {
            val cur = rule.source.removePathTail().toString()
            if (!cur.isNullOrBlank() && (url == cur || url.startsWith(cur + "/")))
                return rule
        }
        return null
    }

    private fun doCache(rule: cacheRule, revision: Long, client: iSvnClient): SVNURL {
        build.activity("caching ${rule.source}").use {
            val cacheTarget = getCacheTarget(rule)
            if (cacheTarget.file != null && !fileHelper.exists(cacheTarget.file)) {
                fileHelper.mkdirs(cacheTarget.file)
                build.message("creating cache repository")
                client.createAndInitialize(rule.source, cacheTarget.file)
            } else {
                client.initializeIfRequired(rule.source, cacheTarget.url)
            }
            build.message("synchronizing to ${cacheTarget.url}")
            doSync(cacheTarget, revision, client)
            return cacheTarget.url
        }
    }

    private fun doSync(cacheTarget: cacheTarget, revision: Long, client: iSvnClient) {
        var delay = 1
        while (true) {
            try {
                if (build.interruptReason() != null) {
                    return
                }
                val lastRevision = client.lastRevision(cacheTarget.url)
                if (lastRevision < revision) {
                    client.synchronize(cacheTarget.url)
                    if (cacheTarget.file != null) {
                        val newLastRevision = client.lastRevision(cacheTarget.url)
                        if (lastRevision / 1000 < newLastRevision / 1000) {
                            build.message("packing")
                            client.pack(cacheTarget.file)
                        }
                    }
                }
                return
            } catch(e: RepositoryLockedException) {
                build.message("repo is locked by ${e.holder}, retry after $delay seconds")
                Thread.sleep((delay * 1000).toLong())
                if (delay < 60)
                    delay++
            }
        }
    }

    private fun getCacheTarget(cacheRule: cacheRule): cacheTarget {
        if (!cacheRule.name.isNullOrBlank()) {
            val customPath = build.agentConfiguration(cachedSubversionConstants.CACHE_PATH_CONFIG_KEY + "." + cacheRule.name)
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
        var root = File(build.agentSystemDirectory(), "cachedSubversion")
        val customPath = build.agentConfiguration(cachedSubversionConstants.CACHE_PATH_CONFIG_KEY)
        if (customPath != null)
            root = File(customPath)
        val cacheFile = File(root, md5(cacheRule.source.toString()).toHexString())
        return cacheTarget(cacheFile)
    }

    private fun getCacheRules(): List<cacheRule> {
        val value = build.config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        var result = listOf<cacheRule>()
        if (value != null)
            result = value.split("\n").map { cacheRule(it.trim()) }
        return result
    }
}