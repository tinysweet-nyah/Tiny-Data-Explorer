package com.tinysweet.dataexplorer.utils

/**
 * Thông tin cơ sở dữ liệu SQLite
 */
data class DatabaseInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: String
)