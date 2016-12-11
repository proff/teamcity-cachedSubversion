package com.proff.teamcity.cachedSubversion

import java.io.File

class fileHelper : iFileHelper {
    override fun mkdirs(file: File): Boolean {
        return file.mkdirs()
    }

    override fun exists(file: File): Boolean {
        return file.exists()
    }

    override fun isDirectory(file: File): Boolean {
        return file.isDirectory
    }

    override fun deleteRecursively(file: File): Boolean {
        return file.deleteRecursively()
    }
}