package com.proff.teamcity.cachedSubversion.svnClient

import com.proff.teamcity.cachedSubversion.iRunningBuild

interface iSvnClientFactory {
    fun create(login: String?, password: String?): svnClient
}