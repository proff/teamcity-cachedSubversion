package com.proff.teamcity.cachedSubversion

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.vcs.CheckoutRules
import jetbrains.buildServer.vcs.VcsRoot
import jetbrains.buildServer.vcs.VcsRootEntry
import junit.framework.TestCase
import kotlin.test.expect

class checkoutSettingsTests : TestCase() {
    fun testDisabled() {
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf<AgentBuildFeature>())
        }

        val settings = checkoutSettings.create(build)

        expect(false) { settings.enabled }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verifyNoMoreInteractions(build)
    }

    fun testEnabled() {
        val params = mapOf<String, String>()
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(false) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun test_revertCheckout() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "revertCheckout"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(true) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun test_checkout() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "checkout"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(false) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun test_deleteExport() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "deleteExport"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Export) { settings.mode }
        expect(false) { settings.revert }
        expect(true) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun test_export() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "export"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Export) { settings.mode }
        expect(false) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testCheckout() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Checkout"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(false) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testCheckoutRevert() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Checkout"), Pair(cachedSubversionConstants.REVERT_CONFIG_KEY, "true"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(true) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testCheckoutClean() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Checkout"), Pair(cachedSubversionConstants.CLEAN_CONFIG_KEY, "true"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(false) { settings.revert }
        expect(true) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testCheckoutRevertClean() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Checkout"),
                Pair(cachedSubversionConstants.REVERT_CONFIG_KEY, "true"),
                Pair(cachedSubversionConstants.CLEAN_CONFIG_KEY, "true"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Checkout) { settings.mode }
        expect(true) { settings.revert }
        expect(true) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testExport() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Export"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Export) { settings.mode }
        expect(false) { settings.revert }
        expect(false) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }

    fun testExportDelete() {
        val params = mapOf(Pair(cachedSubversionConstants.MODE_CONFIG_KEY, "Export"), Pair(cachedSubversionConstants.DELETE_CONFIG_KEY, "true"))
        val feature = mock<AgentBuildFeature> {
            on { parameters } doReturn (params)
        }
        val build = mock<iRunningBuild> {
            on { getBuildFeaturesOfType("cachedSubversion") } doReturn (listOf(feature))
        }

        val settings = checkoutSettings.create(build)

        expect(true) { settings.enabled }
        expect(checkoutMode.Export) { settings.mode }
        expect(false) { settings.revert }
        expect(true) { settings.clean }
        expect(false) { settings.containsOtherVcs }
        expect(0) { settings.vcs.count() }

        verify(build).getBuildFeaturesOfType("cachedSubversion")
        verify(build).vcsRootEntries()
        verifyNoMoreInteractions(build)
    }
}