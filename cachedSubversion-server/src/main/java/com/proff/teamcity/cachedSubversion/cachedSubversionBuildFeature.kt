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
        val sb = StringBuilder()
        sb.append("Will be cache svn repository on agent. Mode is $mode. ")
        if (mode == "Checkout") {
            if (params.containsKey(cachedSubversionConstants.CLEAN_CONFIG_KEY))
                sb.append("Cleanup is enabled. ")
            if (params.containsKey(cachedSubversionConstants.REVERT_CONFIG_KEY))
                sb.append("Revert is enabled. ")
        }
        if (mode == "Export" && params.containsKey(cachedSubversionConstants.DELETE_CONFIG_KEY))
            sb.append("Delete is enabled. ")
        return sb.toString()
    }
}