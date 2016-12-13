package com.proff.teamcity.cachedSubversion

import com.proff.teamcity.cachedSubversion.svnClient.svnClientFactory
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint
import java.io.File
import java.util.*


class cachedSubversion(val agentDispatcher: EventDispatcher<AgentLifeCycleListener>) : AgentLifeCycleAdapter(), PositionAware {
    init {
        agentDispatcher.addListener(this)
    }

    override fun getConstraint(): PositionConstraint {
        return PositionConstraint.between(listOf("swabra"), listOf())
    }

    override fun getOrderId(): String {
        return cachedSubversionConstants.FEATURE_TYPE
    }

    override fun buildStarted(runningBuild: AgentRunningBuild) {
        if (runningBuild.isCleanBuild)
            return
        val build = com.proff.teamcity.cachedSubversion.runningBuild(runningBuild)
        val core = cachedSubversionCore(build, svnClientFactory(runningBuild), checkoutSettings.create(build), cacheHelper(build, fileHelper()), checkoutHelper(build))
        core.run(false)
    }

    override fun dependenciesDownloaded(runningBuild: AgentRunningBuild) {
        //workaround for clean build
        if (!runningBuild.isCleanBuild)
            return
        val tempDir = File(runningBuild.buildTempDirectory, UUID.randomUUID().toString())
        runningBuild.checkoutDirectory.copyRecursively(tempDir, true)
        runningBuild.checkoutDirectory.deleteRecursively()
        val build = com.proff.teamcity.cachedSubversion.runningBuild(runningBuild)
        val core = cachedSubversionCore(build, svnClientFactory(runningBuild), checkoutSettings.create(build), cacheHelper(build, fileHelper()), checkoutHelper(build))
        core.run(false)
        tempDir.copyRecursively(runningBuild.checkoutDirectory, true)
    }

    override fun preparationFinished(runningBuild: AgentRunningBuild) {
    }
}