package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.BuildInterruptReason

interface iRunningBuild {
    fun interruptReason(): BuildInterruptReason?
    fun message(message: String)
    fun warning(message: String)
}