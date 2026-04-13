package com.tinysweet.dataexplorer.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RootUtils - Tiện ích xử lý các thao tác root shell
 * Sử dụng libsu để thực thi lệnh root
 */
object RootUtils {

    /**
     * Kiểm tra thiết bị đã root chưa
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Thực thi lệnh root và trả về kết quả
     */
    suspend fun executeCommand(command: String): List<String> = withContext(Dispatchers.IO) {
        try {
            Shell.cmd(command).exec().out
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Thực thi nhiều lệnh root
     */
    suspend fun executeCommands(commands: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val job = Shell.cmd(*commands.toTypedArray()).exec()
            job.isSuccess
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Đọc nội dung file bằng root
     */
    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("cat '$path'").exec().out.joinToString("\n")
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Ghi nội dung vào file bằng root
     */
    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val escapedContent = content.replace("'", "'\"'\"'")
            Shell.cmd("echo '$escapedContent' > '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Liệt kê thư mục bằng root
     */
    suspend fun listDirectory(path: String): List<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val output = Shell.cmd("ls -la '$path'").exec().out
            parseLsOutput(output)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Parse kết quả ls -la
     */
    private fun parseLsOutput(output: List<String>): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        for (line in output) {
            if (line.startsWith("total") || line.isBlank()) continue
            val parts = line.trim().split(Regex("\\s+"), limit = 9)
            if (parts.size >= 9) {
                val permissions = parts[0]
                val isDirectory = permissions.startsWith("d")
                val name = parts[8]
                val size = if (isDirectory) 0L else parts[4].toLongOrNull() ?: 0L
                files.add(
                    FileInfo(
                        name = name,
                        isDirectory = isDirectory,
                        size = size,
                        permissions = permissions
                    )
                )
            }
        }
        return files
    }

    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("rm -rf '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun copyFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("cp -r '$source' '$destination'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun moveFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("mv '$source' '$destination'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun createDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("mkdir -p '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun zipDirectory(sourceDir: String, outputZip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val parent = sourceDir.substringBeforeLast("/")
            val child = sourceDir.substringAfterLast("/")
            Shell.cmd("cd '$parent' && zip -r '$outputZip' '$child'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unzipFile(zipFile: String, destinationDir: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("unzip -o '$zipFile' -d '$destinationDir'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getDirectorySize(path: String): String = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("du -sh '$path' | cut -f1").exec().out.firstOrNull() ?: "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun closeShell() {
        // No-op: libsu handles shell lifecycle internally.
    }
}
