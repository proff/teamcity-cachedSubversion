package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.svnClientFactory
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint

class cachedSubversionRevert(agentDispatcher: EventDispatcher<AgentLifeCycleListener>) : AgentLifeCycleAdapter(), PositionAware {
    init {
        agentDispatcher.addListener(this)
    }

    override fun getConstraint(): PositionConstraint {
        return PositionConstraint.before("swabra")
    }

    override fun getOrderId(): String {
        return cachedSubversionConstants.FEATURE_TYPE + "Revert"
    }

    override fun buildStarted(runningBuild: AgentRunningBuild) {
        val build = com.proff.teamcity.cachedSubversion.runningBuild(runningBuild)
        val core = cachedSubversionCore(build, svnClientFactory(runningBuild), checkoutSettings.create(build), cacheHelper(build, fileHelper()), checkoutHelper(build))
        core.run(true)
    }
}