package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.cachedSubversionConstants.Companion.MODE_CONFIG_KEY
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class cachedSubversionBuildFeature(val pluginDescriptor: PluginDescriptor) : BuildFeature() {
    init {
    }

    override fun getType(): String {
        return cachedSubversionConstants.FEATURE_TYPE
    }

    override fun getDisplayName(): String {
        return "cache subversion"
    }

    override fun getEditParametersUrl(): String? {
        return pluginDescriptor.getPluginResourcesPath("cachedSubversionSettings.jsp")
    }

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean {
        return false
    }

    override fun describeParameters(params: MutableMap<String, String>): String {
        val mode = params[MODE_CONFIG_KEY]
        return "Will be cache svn repository on agent. Selected checkout mode: $mode"
    }
}