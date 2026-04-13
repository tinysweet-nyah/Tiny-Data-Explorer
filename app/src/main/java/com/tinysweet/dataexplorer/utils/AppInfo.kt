package com.tinysweet.dataexplorer.utils

import android.graphics.Bitmap

/**
 * Thông tin ứng dụng
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val icon: Bitmap?,
    val isSystemApp: Boolean
)