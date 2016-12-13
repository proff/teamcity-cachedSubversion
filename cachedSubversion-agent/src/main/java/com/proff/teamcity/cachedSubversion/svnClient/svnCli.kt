package com.proff.teamcity.cachedSubversion.svnClient

import com.proff.teamcity.cachedSubversion.iRunningBuild
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.wc.SVNRevision
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


class svnCli(val login: String, val password: String, val build: iRunningBuild, authManager: ISVNAuthenticationManager, canceller: () -> Unit) : svnKit(authManager, canceller) {
    private fun exec(nametoLog: String, cmd: String, toLog: String? = null) {
        build.activity("executing $nametoLog").use {
            build.message(toLog ?: cmd)
            val proc = Runtime.getRuntime().exec(cmd)
            var rdr = BufferedReader(InputStreamReader(proc.inputStream))
            rdr.use {
                while (true) {
                    val line = rdr.readLine() ?: break
                    build.message(line)
                }
            }
            rdr = BufferedReader(InputStreamReader(proc.errorStream))
            rdr.use {
                while (true) {
                    val line = rdr.readLine() ?: break
                    build.warning(line)
                }
            }
            val result = proc.waitFor()
            if (result != 0) {
                build.warning("error")
                build.stopBuild("error executing $cmd")
            }
        }
    }

    override fun doCheckout(url: SVNURL, dstPath: File, pegRevision: SVNRevision?, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean): Long {
        if (!isCliExists() || pegRevision != revision || depth != SVNDepth.INFINITY || !allowUnversionedObstructions)
            return super.doCheckout(url, dstPath, pegRevision, revision, depth, allowUnversionedObstructions)
        val cmd = "svn checkout $url $dstPath -r $revision --ignore-externals --username $login"
        exec("svn checkout", "$cmd --password $password", "$cmd --password ******")
        return revision!!.number
    }

    override fun doUpdate(path: File, revision: SVNRevision?, depth: SVNDepth?, allowUnversionedObstructions: Boolean, depthIsSticky: Boolean): Long {
        if (!isCliExists() || depth != SVNDepth.INFINITY || !allowUnversionedObstructions || !depthIsSticky)
            return super.doUpdate(path, revision, depth, allowUnversionedObstructions, depthIsSticky)
        val cmd = "svn update $path -r $revision --ignore-externals --username $login"
        exec("svn update", "$cmd --password $password", "$cmd --password ******")
        return revision!!.number
    }

    override fun doCleanup(path: File) {
        if (!isCliExists())
            return super.doCleanup(path)
        val cmd = "svn cleanup $path --remove-unversioned --remove-ignored --include-externals"
        exec("svn cleanup", cmd)
    }

    override fun doRevert(paths: Array<File>, depth: SVNDepth, changeLists: MutableList<String>) {
        if (!isCliExists() || depth != SVNDepth.INFINITY || paths.count() != 1)
            super.doRevert(paths, depth, changeLists)
        val cmd = "svn revert ${paths.single()} -R"
        exec("svn revert", cmd)
    }

    override fun doExport(url: SVNURL, dstPath: File, pegRevision: SVNRevision, revision: SVNRevision, eolStyle: String?, overwrite: Boolean, depth: SVNDepth): Long {
        if (!isCliExists() || pegRevision != revision || depth != SVNDepth.INFINITY || eolStyle != null || !overwrite)
            return super.doExport(url, dstPath, pegRevision, revision, eolStyle, overwrite, depth)
        val cmd = "svn export -r $revision --ignore-externals --force $url $dstPath"
        exec("svn export", cmd)
        return revision.number
    }

    override fun doSynchronize(toURL: SVNURL, function: (SVNLogEntry) -> Unit) {
        if (!isCliExists())
            return super.doSynchronize(toURL, function)
        val cmd = "svnsync sync $toURL --source-username $login --sync-username $login"
        exec("svnsync sync", "$cmd --source-password $password --sync-password $password", "$cmd --source-password ****** --sync-password ******")
    }

    override fun doPack(repositoryRoot: File) {
        if (!isCliExists())
            return super.doPack(repositoryRoot)
        val cmd = "svnadmin pack $repositoryRoot"
        exec("svnadmin pack", cmd)
    }

    companion object {
        private var cliExists: Boolean? = null
        fun isCliExists(): Boolean {
            var exists = cliExists
            if (exists != null)
                return exists
            try {
                exists = Runtime.getRuntime().exec("svn --version -q").waitFor() == 0
            } catch (e: IOException) {
                exists = false
            }
            cliExists = exists
            return exists ?: false
        }
    }
}