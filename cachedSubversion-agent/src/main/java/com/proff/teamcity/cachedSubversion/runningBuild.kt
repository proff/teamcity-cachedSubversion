package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildInterruptReason

class runningBuild(val build: AgentRunningBuild) : iRunningBuild {
    override fun warning(message: String) {
        build.buildLogger.warning(message)
    }

    override fun message(message: String) {
        build.buildLogger.message(message)
    }

    override fun interruptReason(): BuildInterruptReason? {
        return build.interruptReason
    }
}