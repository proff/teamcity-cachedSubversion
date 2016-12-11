package com.proff.teamcity.cachedSubversion

import jetbrains.buildServer.agent.BuildProgressLogger
import java.io.Closeable

class activity(val logger: BuildProgressLogger, val name: String, val type: String) : Closeable {
    override fun close() {
        logger.activityFinished(name, type)
    }
}