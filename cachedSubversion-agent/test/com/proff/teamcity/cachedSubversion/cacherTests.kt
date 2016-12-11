package com.proff.teamcity.cachedSubversion

import com.nhaarman.mockito_kotlin.*
import com.proff.teamcity.cachedSubversion.svnClient.iSvnClient
import jetbrains.buildServer.agent.BuildInterruptReason
import jetbrains.buildServer.serverSide.cleanup.InterruptReason
import junit.framework.TestCase
import org.mockito.stubbing.OngoingStubbing
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.File

class cacherTests : TestCase() {
    fun testEmptyRules() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
        }
        val client = mock<iSvnClient> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testSimple() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(0, 123))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testCancellation() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
            on { interruptReason() } doReturn (listOf(null, BuildInterruptReason.UNKNOWN_REASON))
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(0, 123))
            on { synchronize(any()) } doThrow (RepositoryLockedException("test"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build, times(2)).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testLocked() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        var count = 0
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(0, 0, 123))
            on { synchronize(any()) }.thenAnswer {
                if (count == 0) {
                    count++
                    throw RepositoryLockedException("test")
                }
            }

        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build, times(2)).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client, times(2)).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testLockedSyncedAfterUnlock() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        var count = 0
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(0, 123))
            on { synchronize(any()) }.thenAnswer {
                if (count == 0) {
                    count++
                    throw RepositoryLockedException("test")
                }
            }

        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build, times(2)).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testNothhingToCache() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (123)
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testPack() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 1123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(1123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(999, 1123))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("agentSystem/cachedSubversion/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(1123))
        verify(client).pack(file)

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testPackHttp() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 1123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com test http://example.org")
            on { agentSystemDirectory() } doReturn (File("agentSystem"))
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(1123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { lastRevision(any()) } doReturn (listOf<Long>(999, 1123))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val url = SVNURL.parseURIEncoded("http://example.org")
        verify(client).initializeIfRequired(SVNURL.parseURIEncoded("http://example.com"), url)
        verify(client, atLeastOnce()).lastRevision(url)
        verify(client).synchronize(url)
        verify(client).getRootUri(vcs.url, SVNRevision.create(1123))

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testCustomAgentSideCachePath() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentConfiguration(cachedSubversionConstants.CACHE_PATH_CONFIG_KEY) } doReturn ("customPath")
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).agentSystemDirectory()
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("customPath/a9b9f04336ce0181a08e774e01113b31")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testCustomAgentSideCachePathHttp() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com test")
            on { agentConfiguration(cachedSubversionConstants.CACHE_PATH_CONFIG_KEY + ".test") } doReturn ("http://example.org")
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val url = SVNURL.parseURIEncoded("http://example.org")
        verify(client).initializeIfRequired(SVNURL.parseURIEncoded("http://example.com"), url)
        verify(client, atLeastOnce()).lastRevision(url)
        verify(client).synchronize(url)
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testDisabledOnAgent() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com")
            on { agentConfiguration(cachedSubversionConstants.DISABLED_CONFIG_KEY) } doReturn ("true")
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testCustomServerSideCachePath() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com test http://example.org")
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val url = SVNURL.parseURIEncoded("http://example.org")
        verify(client).initializeIfRequired(SVNURL.parseURIEncoded("http://example.com"), url)
        verify(client, atLeastOnce()).lastRevision(url)
        verify(client).synchronize(url)
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verifyNoMoreInteractions(build, client, fileHelper)
    }

    fun testCustomServerSideCachePathHttp() {
        val vcs = vcsCheckoutSettings()
        vcs.url = "http://example.com/trunk"
        vcs.login = "test"
        vcs.password = "test"
        vcs.revision = 123
        val build = mock<iRunningBuild> {
            on { config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY) } doReturn ("http://example.com test customServerPath")
        }
        val client = mock<iSvnClient> {
            on { getRootUri("http://example.com/trunk", SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val fileHelper = mock<iFileHelper> {
        }
        val c = cacher(build, fileHelper)

        c.doCache(vcs, client)

        verify(build, atLeast(1)).agentConfiguration(any())
        verify(build).config(cachedSubversionConstants.REPOSITORIES_CONFIG_KEY)
        verify(build).interruptReason()
        verify(build).activity(any(), any())
        verify(build, atLeastOnce()).message(any())

        val file = File("customServerPath")
        verify(client).createAndInitialize(SVNURL.parseURIEncoded("http://example.com"), file)
        verify(client, atLeastOnce()).lastRevision(SVNURL.fromFile(file))
        verify(client).synchronize(SVNURL.fromFile(file))
        verify(client).getRootUri(vcs.url, SVNRevision.create(123))

        verify(fileHelper).exists(file)
        verify(fileHelper).mkdirs(file)

        verifyNoMoreInteractions(build, client, fileHelper)
    }

}
