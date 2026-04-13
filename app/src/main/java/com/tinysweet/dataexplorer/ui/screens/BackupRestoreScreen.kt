package com.tinysweet.dataexplorer.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.utils.RootUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BackupRestoreScreen - Sao lưu và khôi phục dữ liệu ứng dụng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    modifier: Modifier = Modifier,
    appPackageName: String,
    onBack: () -> Unit
) {
    var backups by remember { mutableStateOf<List<BackupInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<BackupInfo?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(appPackageName) {
        isLoading = true
        errorMessage = null
        try {
            backups = loadBackups(context, appPackageName)
        } catch (e: Exception) {
            errorMessage = "Không thể tải bản sao lưu: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBackupDialog = true },
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                text = { Text("Sao lưu") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                backups.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có bản sao lưu nào",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Nhấn nút Sao lưu để tạo bản sao lưu mới",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backups, key = { it.fileName }) { backup ->
                            BackupItem(
                                backup = backup,
                                onRestore = {
                                    selectedBackup = backup
                                    showRestoreDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            deleteBackup(context, backup)
                                            backups = loadBackups(context, appPackageName)
                                        } catch (e: Exception) {
                                            errorMessage = "Xóa bản sao lưu thất bại: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        BackupDialog(
            onBackup = { backupName ->
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val created = createBackup(context, appPackageName, backupName)
                        if (created) {
                            backups = loadBackups(context, appPackageName)
                            showBackupDialog = false
                        } else {
                            errorMessage = "Sao lưu thất bại"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Sao lưu thất bại: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            onDismiss = { showBackupDialog = false }
        )
    }

    if (showRestoreDialog && selectedBackup != null) {
        RestoreDialog(
            backup = selectedBackup!!,
            onRestore = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val restored = restoreBackup(context, appPackageName, selectedBackup!!)
                        if (restored) {
                            showRestoreDialog = false
                        } else {
                            errorMessage = "Khôi phục thất bại"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Khôi phục thất bại: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
fun BackupItem(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ngày tạo: ${backup.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kích thước: ${formatFileSize(backup.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Khôi phục")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                }
            }
        }
    }
}

@Composable
fun BackupDialog(
    onBackup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var backupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo bản sao lưu") },
        text = {
            Column {
                Text(
                    text = "Đặt tên cho bản sao lưu:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("VD: backup_2024_01_01") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (backupName.isNotBlank()) {
                        onBackup(backupName.trim())
                    }
                }
            ) {
                Text("Sao lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun RestoreDialog(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận khôi phục") },
        text = {
            Column {
                Text(
                    text = "Bạn có chắc chắn muốn khôi phục dữ liệu từ bản sao lưu '${backup.name}'?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cảnh báo: Dữ liệu hiện tại sẽ bị ghi đè!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRestore,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Khôi phục")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

/**
 * Tạo bản sao lưu dữ liệu ứng dụng
 */
suspend fun createBackup(
    context: Context,
    appPackageName: String,
    backupName: String
): Boolean {
    val dataPath = "/data/data/$appPackageName"
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")
    if (!backupDir.exists()) {
        backupDir.mkdirs()
    }

    val backupFile = File(backupDir, "$backupName.zip")
    val success = RootUtils.zipDirectory(dataPath, backupFile.absolutePath)

    if (success) {
        val infoFile = File(backupDir, "$backupName.info")
        infoFile.writeText(
            "name=$backupName\n" +
                "date=${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n" +
                "package=$appPackageName\n" +
                "size=${backupFile.length()}"
        )
    }

    return success
}

/**
 * Khôi phục dữ liệu từ bản sao lưu
 */
suspend fun restoreBackup(
    context: Context,
    appPackageName: String,
    backup: BackupInfo
): Boolean {
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")
    val backupFile = File(backupDir, backup.fileName)
    val dataPath = "/data/data/$appPackageName"

    RootUtils.deleteFile(dataPath)
    RootUtils.createDirectory(dataPath)
    return RootUtils.unzipFile(backupFile.absolutePath, dataPath)
}

/**
 * Tải danh sách bản sao lưu
 */
fun loadBackups(context: Context, appPackageName: String): List<BackupInfo> {
    val backups = mutableListOf<BackupInfo>()
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")

    if (backupDir.exists()) {
        backupDir.listFiles { file -> file.extension.equals("zip", ignoreCase = true) }
            ?.forEach { zipFile ->
                val infoFile = File(backupDir, "${zipFile.nameWithoutExtension}.info")
                val info = if (infoFile.exists()) {
                    infoFile.readLines().mapNotNull { line ->
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()
                } else {
                    emptyMap()
                }

                backups.add(
                    BackupInfo(
                        name = info["name"] ?: zipFile.nameWithoutExtension,
                        fileName = zipFile.name,
                        date = info["date"] ?: "Unknown",
                        size = info["size"]?.toLongOrNull() ?: zipFile.length(),
                        packageName = info["package"] ?: appPackageName
                    )
                )
            }
    }

    return backups.sortedByDescending { it.date }
}

/**
 * Xóa bản sao lưu
 */
suspend fun deleteBackup(context: Context, backup: BackupInfo): Boolean {
    val backupDir = File(context.getExternalFilesDir(null), "backups/${backup.packageName}")
    val zipFile = File(backupDir, backup.fileName)
    val infoFile = File(backupDir, "${backup.fileName.substringBeforeLast(".")}.info")

    val zipDeleted = !zipFile.exists() || zipFile.delete()
    val infoDeleted = !infoFile.exists() || infoFile.delete()
    return zipDeleted && infoDeleted
}

fun formatFileSize(size: Long): String = when {
    size < 1024L -> "$size B"
    size < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", size / 1024f)
    size < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", size / (1024f * 1024f))
    else -> String.format(Locale.US, "%.1f GB", size / (1024f * 1024f * 1024f))
}

/**
 * Thông tin bản sao lưu
 */
data class BackupInfo(
    val name: String,
    val fileName: String,
    val date: String,
    val size: Long,
    val packageName: String
)
