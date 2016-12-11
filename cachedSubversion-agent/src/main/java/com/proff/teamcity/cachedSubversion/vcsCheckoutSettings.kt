package com.proff.teamcity.cachedSubversion

class vcsCheckoutSettings {
    var url: String = ""
    var login: String? = null
    var password: String? = null
    var revision: Long = 0
    val rules: MutableList<checkoutRule> = mutableListOf()
}