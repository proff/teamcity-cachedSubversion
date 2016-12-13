package com.proff.teamcity.cachedSubversion

class checkoutRule(rule: String) {
    var exclude: Boolean = false
    var from: String = ""
    var to: String = ""
    var value: String = ""

    init {
        var r = rule.trim()
        if (!r.isNullOrBlank()) {
            if (r.startsWith("-:")) {
                exclude = true
                r = r.removePrefix("-:").trim()
            } else if (r.startsWith("+:")) {
                exclude = false
                r = r.removePrefix("+:")
            } else {
                exclude = false
            }
            val parts = r.split("=>", limit = 2)
            from = normalize(parts[0])
            to = normalize(if (parts.count() > 1) parts[1] else from)
            value = rule
        } else {
            value = ".=>."
        }
    }

    private fun normalize(s: String): String {
        return s.trim().trimStart('.').replace("\\", "/").trim('/')
    }

    override fun toString(): String {
        return value
    }
}