package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildInterruptReason
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.vcs.VcsRoot
import jetbrains.buildServer.vcs.VcsRootEntry
import java.io.Closeable
import java.io.File

class runningBuild(val build: AgentRunningBuild) : iRunningBuild {
    override fun agentSystemDirectory(): File {
        return build.agentConfiguration.systemDirectory
    }

    override fun agentConfiguration(name: String): String? {
        return build.agentConfiguration.configurationParameters[name]
    }

    override fun config(name: String): String? {
        return build.sharedConfigParameters[name]
    }

    override fun stopBuild(message: String) {
        build.stopBuild(message)
    }

    override fun checkoutDirectory(): File {
        return build.checkoutDirectory
    }

    override fun getBuildCurrentVersion(vcsRoot: VcsRoot): String {
        return build.getBuildCurrentVersion(vcsRoot)
    }

    override fun vcsRootEntries(): List<VcsRootEntry> {
        return build.vcsRootEntries
    }

    override fun activity(name: String, type: String): Closeable {
        build.buildLogger.activityStarted(name, type)
        return com.proff.teamcity.cachedSubversion.activity(build.buildLogger, name, type)
    }

    override fun checkoutType(): AgentBuild.CheckoutType {
        return build.checkoutType
    }

    override fun getBuildFeaturesOfType(type: String): List<AgentBuildFeature> {
        return build.getBuildFeaturesOfType(type).toList()
    }

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