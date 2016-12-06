package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.ServerPaths
import java.io.File

class cachedSubversionPropertiesProvider(extensionHolder: ExtensionHolder, val paths: ServerPaths) : BuildStartContextProcessor {
    init {
        extensionHolder.registerExtension(BuildStartContextProcessor::class.java, "cachedSubversionPropertiesProvider", this)
    }


    override fun updateParameters(buildStartContext: BuildStartContext) {
        val config = File(paths.configDir, cachedSubversionConstants.REPOSITORIES_CONFIG_FILE)
        if (!config.exists())
            config.writeText("")
        else
            buildStartContext.addSharedParameter(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY, config.readText())
    }
}