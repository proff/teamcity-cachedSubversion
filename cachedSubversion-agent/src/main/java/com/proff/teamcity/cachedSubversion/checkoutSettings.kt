package com.proff.teamcity.cachedSubversion

class checkoutSettings() {
    var mode: checkoutMode = checkoutMode.Checkout
    var revert: Boolean = false
    var clean: Boolean = false
    var containsOtherVcs = false
    var enabled = false
    val vcs = mutableListOf<vcsCheckoutSettings>()

    companion object {
        fun create(build: iRunningBuild): checkoutSettings {
            val result = checkoutSettings()
            val feature = build.getBuildFeaturesOfType("cachedSubversion")
            result.enabled = feature.any()
            if (result.enabled) {
                val parameters = feature.single().parameters
                when (parameters[cachedSubversionConstants.MODE_CONFIG_KEY]) {
                    "revertCheckout" -> {// obsolete value
                        result.mode = checkoutMode.Checkout
                        result.revert = true
                        result.clean = false
                    }
                    "checkout" -> {// obsolete value
                        result.mode = checkoutMode.Checkout
                        result.revert = false
                        result.clean = false
                    }
                    "deleteExport" -> {//obsolete value
                        result.mode = checkoutMode.Export
                        result.clean = true
                    }
                    "export" -> {// obsolete value
                        result.mode = checkoutMode.Export
                        result.clean = false
                    }
                    "Checkout" -> {
                        result.mode = checkoutMode.Checkout
                    }
                    "Export" -> {
                        result.mode = checkoutMode.Export
                    }
                }
                if (result.mode == checkoutMode.Checkout) {
                    if (parameters.containsKey(cachedSubversionConstants.REVERT_CONFIG_KEY))
                        result.revert = true
                    if (parameters.containsKey(cachedSubversionConstants.CLEAN_CONFIG_KEY))
                        result.clean = true
                } else {
                    if (parameters.containsKey(cachedSubversionConstants.DELETE_CONFIG_KEY))
                        result.clean = true
                }
                for (entry in build.vcsRootEntries()) {
                    val type = entry.vcsRoot.vcsName
                    if (type != "svn") {
                        result.containsOtherVcs = true
                        continue
                    }
                    val vcs = vcsCheckoutSettings()
                    vcs.url = entry.properties["url"]!!
                    vcs.revision = build.getBuildCurrentVersion(entry.vcsRoot).split("_")[0].toLong()
                    vcs.login = entry.properties["user"]
                    vcs.password = entry.properties["secure:svn-password"]
                    for (rule in entry.checkoutRulesSpecification.split("\n")) {
                        vcs.rules.add(checkoutRule(rule))
                    }
                    if (vcs.rules.isEmpty()) {
                        vcs.rules.add(checkoutRule(".=>."))
                    }
                    result.vcs.add(vcs)
                }
            }
            return result
        }
    }

    constructor(mode: checkoutMode, revert: Boolean, clean: Boolean) : this() {
        this.mode = mode
        this.revert = revert
        this.clean = clean
    }
}