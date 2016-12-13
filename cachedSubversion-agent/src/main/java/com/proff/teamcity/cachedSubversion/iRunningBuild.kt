package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.BuildInterruptReason
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.vcs.VcsRoot
import jetbrains.buildServer.vcs.VcsRootEntry
import java.io.Closeable
import java.io.File

interface iRunningBuild {
    fun interruptReason(): BuildInterruptReason?
    fun message(message: String)
    fun warning(message: String)
    fun getBuildFeaturesOfType(type: String): List<AgentBuildFeature>
    fun checkoutType(): AgentBuild.CheckoutType
    fun activity(name: String): Closeable
    fun vcsRootEntries(): List<VcsRootEntry>
    fun getBuildCurrentVersion(vcsRoot: VcsRoot): String
    fun checkoutDirectory(): File
    fun stopBuild(message: String)
    fun config(name: String): String?
    fun agentConfiguration(name: String): String?
    fun agentSystemDirectory(): File
}