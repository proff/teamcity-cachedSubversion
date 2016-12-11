package com.proff.teamcity.cachedSubversion.svnClient

import com.proff.teamcity.cachedSubversion.iRunningBuild
import com.proff.teamcity.cachedSubversion.runningBuild
import jetbrains.buildServer.agent.AgentRunningBuild

class svnClientFactory(val build: AgentRunningBuild) : iSvnClientFactory {
    override fun create(login: String?, password: String?): svnClient {
        return svnClient.create(login, password, build)
    }
}