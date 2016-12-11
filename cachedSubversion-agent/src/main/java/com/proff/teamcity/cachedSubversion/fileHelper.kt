package com.proff.teamcity.cachedSubversion

import java.io.File

class fileHelper : iFileHelper {
    override fun isDirectory(file: File): Boolean {
        return file.isDirectory
    }

    override fun deleteRecursively(file: File): Boolean {
        return file.deleteRecursively()
    }
}