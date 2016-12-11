package com.proff.teamcity.cachedSubversion

import java.io.File

interface iFileHelper {
    fun deleteRecursively(file: File): Boolean
    fun isDirectory(file: File): Boolean
    fun exists(file: File): Boolean
    fun mkdirs(file: File): Boolean
}