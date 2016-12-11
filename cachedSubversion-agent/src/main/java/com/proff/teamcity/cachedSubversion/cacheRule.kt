package com.proff.teamcity.cachedSubversion

import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import java.io.File

class cacheRule(rule: String) {
    val source: SVNURL
    val name: String?
    val target: cacheTarget?

    init {
        val arr = rule.split(' ')
        source = SVNURL.parseURIEncoded(arr[0])
        name = if (arr.count() > 1) arr[1] else null
        if (arr.count() > 2) {
            var t: cacheTarget
            try {
                t = cacheTarget(SVNURL.parseURIEncoded(arr[2]))
            } catch(e: SVNException) {
                //is not url, try parse as File
                t = cacheTarget(File(arr[2]))
            }
            target = t
        } else
            target = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as cacheRule

        if (source != other.source) return false
        if (name != other.name) return false
        if (target != other.target) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (target?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "cacheRule(source=$source, name=$name, target=$target)"
    }
}