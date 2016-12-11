package com.proff.teamcity.cachedSubversion

import org.tmatesoft.svn.core.SVNURL
import java.io.File

class cacheTarget {
    var url: SVNURL
    val file: File?

    constructor(url: SVNURL) {
        this.url = url
        this.file = null
    }

    constructor(file: File) {
        this.file = file
        this.url = SVNURL.fromFile(file)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as cacheTarget

        if (url != other.url) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "cacheTarget(url=$url, file=$file)"
    }


}