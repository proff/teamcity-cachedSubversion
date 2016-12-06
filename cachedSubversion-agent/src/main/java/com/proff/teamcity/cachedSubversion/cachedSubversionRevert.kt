package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
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
        run(runningBuild, true)
    }
}