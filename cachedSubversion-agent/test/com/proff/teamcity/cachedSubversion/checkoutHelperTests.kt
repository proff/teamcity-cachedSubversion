package com.proff.teamcity.cachedSubversion

import com.nhaarman.mockito_kotlin.*
import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import junit.framework.TestCase
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

class checkoutHelperTests : TestCase() {
    fun testEmptyRulesBeforeSwabra() {
        val build = mock<iRunningBuild> {
        }
        val client = mock<iSvnClient> {
        }

        val helper = checkoutHelper(build)

        helper.doCheckout(SVNURL.parseURIEncoded("http://example.com"), checkoutSettings(), vcsCheckoutSettings(), client, true)

        verifyNoMoreInteractions(build, client)
    }

    fun testEmptyRulesAfterSwabra() {
        val build = mock<iRunningBuild> {
        }
        val client = mock<iSvnClient> {
        }

        val helper = checkoutHelper(build)

        helper.doCheckout(SVNURL.parseURIEncoded("http://example.com"), checkoutSettings(), vcsCheckoutSettings(), client, false)

        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabra() {
        val settings = checkoutSettings()
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabra() {
        val settings = checkoutSettings()
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraClean() {
        val settings = checkoutSettings()
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraClean() {
        val settings = checkoutSettings()
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).cleanup(File("checkoutDir"))
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraCleanExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraCleanExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraRevert() {
        val settings = checkoutSettings()
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraRevert() {
        val settings = checkoutSettings()
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).revert(File("checkoutDir"))
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraRevertExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.clean = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraRevertExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraCleanRevert() {
        val settings = checkoutSettings()
        settings.clean = true
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraCleanRevert() {
        val settings = checkoutSettings()
        settings.clean = true
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).revert(File("checkoutDir"))
        verify(client).cleanup(File("checkoutDir"))
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleAfterSwabraCleanRevertExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.clean = true
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verify(client).checkout(url, File("checkoutDir"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }

    fun testSimpleRuleBeforeSwabraCleanRevertExport() {
        val settings = checkoutSettings()
        settings.mode = checkoutMode.Export
        settings.clean = true
        settings.revert = true
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule(""))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, true)

        verify(build).message(".=>.")
        verify(build).checkoutDirectory()
        verifyNoMoreInteractions(build, client)
    }

    fun testSeveralRules() {
        val settings = checkoutSettings()
        val vcs = vcsCheckoutSettings()
        vcs.revision = 123
        settings.vcs.add(vcs)
        vcs.rules.add(checkoutRule("test"))
        vcs.rules.add(checkoutRule("test1=>."))
        vcs.rules.add(checkoutRule("./test2 =>test2/sdfsdfsd/sdfsdg\\afgvdv"))
        vcs.rules.add(checkoutRule("+:test3=> ./test3"))
        vcs.rules.add(checkoutRule("-:test4"))
        vcs.rules.add(checkoutRule("+: . => test5/"))
        val build = mock<iRunningBuild> {
            on { checkoutDirectory() } doReturn (File("checkoutDir"))
        }
        val client = mock<iSvnClient> {
        }
        val helper = checkoutHelper(build)
        val url = SVNURL.parseURIEncoded("http://example.com")

        helper.doCheckout(url, settings, vcs, client, false)

        verify(build, atLeastOnce()).message(any())
        verify(build).warning("exclude rule is ignored: -:test4")
        verify(build, atLeastOnce()).checkoutDirectory()
        val file = File("checkoutDir")
        verify(client).checkout(url.appendPath("test", false), File(file, "test"), SVNRevision.create(123), settings)
        verify(client).checkout(url.appendPath("test1", false), file, SVNRevision.create(123), settings)
        verify(client).checkout(url.appendPath("test2", false), File(file, "test2/sdfsdfsd/sdfsdg/afgvdv"), SVNRevision.create(123), settings)
        verify(client).checkout(url.appendPath("test3", false), File(file, "test3"), SVNRevision.create(123), settings)
        verify(client).checkout(url, File(file, "test5"), SVNRevision.create(123), settings)
        verifyNoMoreInteractions(build, client)
    }
}