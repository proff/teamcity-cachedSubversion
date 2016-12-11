import com.proff.teamcity.cachedSubversion.cacheRule
import com.proff.teamcity.cachedSubversion.cacheTarget
import junit.framework.TestCase
import org.tmatesoft.svn.core.SVNURL
import java.io.File
import kotlin.test.expect

class cacheRuleTests : TestCase() {
    fun testShouldParseV1Value() {
        val rule = cacheRule("http://example.org")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect(null) { rule.name }
        expect(null) { rule.target }
    }

    fun testShouldParseV2ValueUrl() {
        val rule = cacheRule("http://example.org example http://example.com")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(SVNURL.parseURIEncoded("http://example.com"))) { rule.target }
    }

    fun testShouldParseV2ValueLocalWindowsPath() {
        val rule = cacheRule("http://example.org example c:\\test/cache")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(File("c:\\test\\cache"))) { rule.target }
    }

    fun testShouldParseV2ValueLocalNixPath() {
        val rule = cacheRule("http://example.org example /home/cache")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(File("/home/cache"))) { rule.target }
    }

    fun testShouldParseV2ValueNetworkWindowsPath() {
        val rule = cacheRule("http://example.org example \\\\test/cache")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(File("//test\\cache"))) { rule.target }
    }

    fun testShouldParseV2ValueNetworkNixPath() {
        val rule = cacheRule("http://example.org example //test/cache")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(File("//test/cache"))) { rule.target }
    }

    fun testShouldIgnoreRedundantValues() {
        val rule = cacheRule("http://example.org example http://example.com dfgdfgdfgdfg")
        expect(SVNURL.parseURIEncoded("http://example.org")) { rule.source }
        expect("example") { rule.name }
        expect(cacheTarget(SVNURL.parseURIEncoded("http://example.com"))) { rule.target }
    }
}