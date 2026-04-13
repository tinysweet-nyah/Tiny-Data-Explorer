package com.tinysweet.dataexplorer.utils

/**
 * Thông tin file SharedPreferences
 */
data class SharedPreferencesInfo(
    val name: String,
    val filePath: String,
    val lastModified: String
)