package com.proff.teamcity.cachedSubversionTests

import com.nhaarman.mockito_kotlin.*
import com.proff.teamcity.cachedSubversion.*
import com.proff.teamcity.cachedSubversion.svnClient.iSvnKit
import com.proff.teamcity.cachedSubversion.svnClient.svnClient
import junit.framework.TestCase
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.wc.SVNInfo
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNStatus
import org.tmatesoft.svn.core.wc.admin.SVNSyncInfo
import java.io.File
import java.util.*

class svnClientTests : TestCase() {
    fun testCreateAndInitialize() {
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val from = SVNURL.parseURIEncoded("http://example.org")
        val to = File("abccba")
        client.createAndInitialize(from, to)

        verify(kit, times(1)).doCreateRepository(eq(to), check { UUID.fromString(it) }, eq(true), eq(false))
        verify(kit, times(1)).doInitialize(eq(from), eq(SVNURL.fromFile(to)))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testInitializeIfRequiredNotInitialized() {
        val from = SVNURL.parseURIEncoded("http://example.org")
        val to = SVNURL.parseURIEncoded("http://example.com")
        val kit = mock<iSvnKit> {
            on { getRevisionProperties(to, 0, null) } doReturn (SVNProperties())
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.initializeIfRequired(from, to)

        verify(kit, times(1)).getRevisionProperties(to, 0, null)
        verify(kit, times(1)).doInitialize(from, to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testInitializeIfRequiredInitialized() {
        val from = SVNURL.parseURIEncoded("http://example.org")
        val to = SVNURL.parseURIEncoded("http://example.com")
        val result = SVNProperties()
        result.put("svn:sync-from-uuid", UUID.randomUUID().toString())
        val kit = mock<iSvnKit> {
            on { getRevisionProperties(to, 0, null) } doReturn (result)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.initializeIfRequired(from, to)

        verify(kit, times(1)).getRevisionProperties(to, 0, null)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testSynchronizeUnlocked() {
        val url = SVNURL.parseURIEncoded("http://example.com")
        val kit = mock<iSvnKit> {
            on { getRevisionProperties(url, 0, null) } doReturn (SVNProperties())
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.synchronize(url)

        verify(kit, times(1)).getRevisionProperties(url, 0, null)
        verify(kit, times(1)).doSynchronize(eq(url), any())
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testSynchronizeLocked() {
        val url = SVNURL.parseURIEncoded("http://example.com")
        val result = SVNProperties()
        result.put("svn:sync-lock", "sdfsdfsfsfsd")
        val kit = mock<iSvnKit> {
            on { getRevisionProperties(url, 0, null) } doReturn (result)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        var b = false
        try {
            client.synchronize(url)
        } catch (e: RepositoryLockedException) {
            b = true
        }
        assert(b)

        verify(kit, times(1)).getRevisionProperties(url, 0, null)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testSynchronizeLockedAfterCheck() {
        val url = SVNURL.parseURIEncoded("http://example.com")

        val childError = SVNErrorMessage.create(SVNErrorCode.getErrorCode(204899), "Failed to get lock on destination repos, currently held by 'test'")
        val exception = SVNException(childError.wrap("sdfsdfsdf"))

        val kit = mock<iSvnKit> {
            on { getRevisionProperties(url, 0, null) } doReturn (SVNProperties())
            on { doSynchronize(eq(url), any()) } doThrow (exception)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        var b = false
        try {
            client.synchronize(url)
        } catch (e: RepositoryLockedException) {
            b = true
        }
        assert(b)

        verify(kit, times(1)).getRevisionProperties(url, 0, null)
        verify(kit, times(1)).doSynchronize(eq(url), any())
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testSynchronizeUnlockedLogging() {
        val url = SVNURL.parseURIEncoded("http://example.com")
        val kit = mock<iSvnKit> {
            on { getRevisionProperties(url, 0, null) } doReturn (SVNProperties())
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.synchronize(url)
        argumentCaptor<(SVNLogEntry) -> Unit>().apply {
            verify(kit).doSynchronize(any(), capture())
            firstValue(SVNLogEntry(mutableMapOf<Any, Any>(), 123, "abccba", Date(), ""))
        }


        verify(kit, times(1)).getRevisionProperties(url, 0, null)
        verify(kit, times(1)).doSynchronize(eq(url), any())
        verify(build, times(1)).message("123")
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testPack() {
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.pack(File("abccba"))


        verify(kit, times(1)).doPack(File("abccba"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testLastRevision() {
        val url = SVNURL.parseURIEncoded("http://example.com")
        val kit = mock<iSvnKit> {
            on { doInfo(url) } doReturn (SVNSyncInfo(url.toString(), UUID.randomUUID().toString(), 123.toLong()))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val rev = client.lastRevision(url)

        assert(rev == 123.toLong())

        verify(kit, times(1)).doInfo(url)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutCheckout() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(fileHelper, times(1)).isDirectory(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutCheckoutRevert() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, true, false))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(fileHelper, times(1)).isDirectory(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutCheckoutClean() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, true))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(fileHelper, times(1)).isDirectory(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutCheckoutRevertClean() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, true, true))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(fileHelper, times(1)).isDirectory(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutExport() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Export, false, false))

        verify(kit, times(1)).doExport(from, to, SVNRevision.create(123), SVNRevision.create(123), null, true, SVNDepth.INFINITY)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutExportClean() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { deleteRecursively(to) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Export, false, true))

        verify(kit, times(1)).doExport(from, to, SVNRevision.create(123), SVNRevision.create(123), null, true, SVNDepth.INFINITY)
        verify(fileHelper, times(1)).deleteRecursively(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutExportCleanFileUsed() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { deleteRecursively(to) } doReturn (false)
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Export, false, true))

        verify(kit, times(1)).doExport(from, to, SVNRevision.create(123), SVNRevision.create(123), null, true, SVNDepth.INFINITY)
        verify(build, times(1)).warning("Directory $to can't be completely deleted")
        verify(fileHelper, times(1)).deleteRecursively(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutDirectoryExistsNotWorkingCopy() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(to) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(fileHelper, times(1)).isDirectory(to)
        verify(fileHelper, times(1)).isDirectory(File(to, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutDirectoryExistsWorkingCopy() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val status = SVNStatus()
        status.remoteURL = from
        val kit = mock<iSvnKit> {
            on { doStatus(to, false) } doReturn (status)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(to) } doReturn (true)
            on { isDirectory(File(to, ".svn")) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))

        verify(kit, times(1)).doUpdate(to, SVNRevision.create(123), SVNDepth.INFINITY, true, true)
        verify(kit, times(1)).doStatus(to, false)
        verify(fileHelper, times(1)).isDirectory(to)
        verify(fileHelper, times(1)).isDirectory(File(to, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutDirectoryExistsWorkingCopyOtherUrl() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val status = SVNStatus()
        status.remoteURL = from.appendPath("sdfsdfsd", false)
        val kit = mock<iSvnKit> {
            on { doStatus(to, false) } doReturn (status)
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(to) } doReturn (true)
            on { isDirectory(File(to, ".svn")) } doReturn (true)
            on { deleteRecursively(to) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))

        verify(kit, times(1)).doCheckout(from, to, SVNRevision.create(123), SVNRevision.create(123), SVNDepth.INFINITY, true)
        verify(kit, times(1)).doStatus(to, false)
        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verify(fileHelper, times(1)).isDirectory(to)
        verify(fileHelper, times(1)).isDirectory(File(to, ".svn"))
        verify(fileHelper, times(1)).deleteRecursively(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutDirectoryExistsWorkingCopyOtherUrlFileUsed() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/")
        val to = File("abccba")
        val status = SVNStatus()
        status.remoteURL = from.appendPath("sdfsdfsd", false)
        val kit = mock<iSvnKit> {
            on { doStatus(to, false) } doReturn (status)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(to) } doReturn (true)
            on { isDirectory(File(to, ".svn")) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        var b = false
        try {
            client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))
        } catch (e: cachedSubversionException) {
            b = true
        }
        assert(b)

        verify(kit, times(1)).doStatus(to, false)
        verify(fileHelper, times(1)).isDirectory(to)
        verify(fileHelper, times(1)).isDirectory(File(to, ".svn"))
        verify(fileHelper, times(1)).deleteRecursively(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCheckoutFile() {
        val from = SVNURL.parseURIEncoded("http://example.com/test/test.txt")
        val to = File("abccba")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, from, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com/"))
            on { checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test/test.txt", SVNRevision.create(123)) } doReturn (SVNNodeKind.FILE)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.checkout(from, to, SVNRevision.create(123), checkoutSettings(checkoutMode.Checkout, false, false))

        verify(kit, times(1)).getReposRoot(null, from, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test/test.txt", SVNRevision.create(123))
        verify(kit, times(1)).doExport(from, to, SVNRevision.create(123), SVNRevision.create(123), null, true, SVNDepth.INFINITY)
        verify(fileHelper, times(1)).isDirectory(to)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testGetRootUriString() {
        val url = "http://example.com/test/"
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, SVNURL.parseURIEncoded(url), SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.getRootUri(url, SVNRevision.create(123))

        assert(result == SVNURL.parseURIEncoded("http://example.com"))

        verify(kit, times(1)).getReposRoot(null, SVNURL.parseURIEncoded(url), SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testGetRootUriUrl() {
        val url = SVNURL.parseURIEncoded("http://example.com/test/")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, url, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.getRootUri(url, SVNRevision.create(123))

        assert(result == SVNURL.parseURIEncoded("http://example.com"))

        verify(kit, times(1)).getReposRoot(null, url, SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testRevertNotExists() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.revert(file)

        verify(fileHelper, times(1)).isDirectory(file)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testRevertExistsNotWorkingCopy() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(file) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.revert(file)

        verify(fileHelper, times(1)).isDirectory(file)
        verify(fileHelper, times(1)).isDirectory(File(file, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testRevertExistsWorkingCopy() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(file) } doReturn (true)
            on { isDirectory(File(file, ".svn")) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.revert(file)

        verify(kit, times(1)).doRevert(arrayOf(file), SVNDepth.INFINITY, mutableListOf())
        verify(fileHelper, times(1)).isDirectory(file)
        verify(fileHelper, times(1)).isDirectory(File(file, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCleanupNotExists() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        client.cleanup(file)

        verify(fileHelper, times(1)).isDirectory(file)
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCleanupExistsNotWorkingCopy() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(file) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.cleanup(file)

        verify(fileHelper, times(1)).isDirectory(file)
        verify(fileHelper, times(1)).isDirectory(File(file, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testCleanupExistsWorkingCopy() {
        val file = File("abccba")
        val kit = mock<iSvnKit> {
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
            on { isDirectory(file) } doReturn (true)
            on { isDirectory(File(file, ".svn")) } doReturn (true)
        }
        val client = svnClient(kit, build, fileHelper)

        client.cleanup(file)

        verify(kit, times(1)).doCleanup(file)
        verify(fileHelper, times(1)).isDirectory(file)
        verify(fileHelper, times(1)).isDirectory(File(file, ".svn"))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testIsFileDir() {
        val url = SVNURL.parseURIEncoded("http://example.com/test/")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, url, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123)) } doReturn (SVNNodeKind.DIR)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.isFile(url, SVNRevision.create(123))

        assert(!result)
        verify(kit, times(1)).getReposRoot(null, url, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testIsFileFile() {
        val url = SVNURL.parseURIEncoded("http://example.com/test/")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, url, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123)) } doReturn (SVNNodeKind.FILE)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.isFile(url, SVNRevision.create(123))

        assert(result)
        verify(kit, times(1)).getReposRoot(null, url, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testIsFileNone() {
        val url = SVNURL.parseURIEncoded("http://example.com/test/")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, url, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123)) } doReturn (SVNNodeKind.NONE)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.isFile(url, SVNRevision.create(123))

        assert(!result)
        verify(kit, times(1)).getReposRoot(null, url, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }

    fun testIsFileUnknown() {
        val url = SVNURL.parseURIEncoded("http://example.com/test/")
        val kit = mock<iSvnKit> {
            on { getReposRoot(null, url, SVNRevision.create(123)) } doReturn (SVNURL.parseURIEncoded("http://example.com"))
            on { checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123)) } doReturn (SVNNodeKind.UNKNOWN)
        }
        val build = mock<iRunningBuild> {
        }
        val fileHelper = mock<iFileHelper> {
        }
        val client = svnClient(kit, build, fileHelper)

        val result = client.isFile(url, SVNRevision.create(123))

        assert(!result)
        verify(kit, times(1)).getReposRoot(null, url, SVNRevision.create(123))
        verify(kit, times(1)).checkPath(SVNURL.parseURIEncoded("http://example.com"), "/test", SVNRevision.create(123))
        verifyNoMoreInteractions(kit, build, fileHelper)
    }
}