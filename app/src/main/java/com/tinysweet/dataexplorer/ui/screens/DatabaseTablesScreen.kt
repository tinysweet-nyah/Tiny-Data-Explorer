package com.tinysweet.dataexplorer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.utils.RootUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTablesScreen(
    modifier: Modifier = Modifier,
    databasePath: String,
    databaseName: String,
    onBack: () -> Unit = {}
) {
    var tables by remember { mutableStateOf<List<TableInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTable by remember { mutableStateOf<TableInfo?>(null) }

    LaunchedEffect(databasePath) {
        isLoading = true
        errorMessage = null
        try {
            tables = loadTables(databasePath)
        } catch (e: Exception) {
            errorMessage = "Không thể tải bảng: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (selectedTable != null) {
        TableDataScreen(
            databasePath = databasePath,
            tableName = selectedTable!!.name,
            onBack = { selectedTable = null }
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(databaseName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
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

                tables.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Không tìm thấy bảng nào",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tables, key = { it.name }) { table ->
                            TableCard(
                                table = table,
                                onClick = { selectedTable = table }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCard(
    table: TableInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${table.rowCount} hàng, ${table.columns.size} cột",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(
    databasePath: String,
    tableName: String,
    onBack: () -> Unit
) {
    var columns by remember { mutableStateOf<List<String>>(emptyList()) }
    var rows by remember { mutableStateOf<List<List<Any?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    var showQueryDialog by remember { mutableStateOf(false) }
    var customQueryResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tableName, databasePath) {
        isLoading = true
        try {
            val schemaQuery = "sqlite3 '$databasePath' \".schema $tableName\""
            val schema = RootUtils.executeCommand(schemaQuery).joinToString("\n")
            columns = getColumnsFromSchema(schema)

            val selectQuery = "sqlite3 '$databasePath' -separator '|' \"SELECT * FROM $tableName LIMIT 500\""
            val data = RootUtils.executeCommand(selectQuery).joinToString("\n")
            rows = parseSqliteData(data, columns.size)
        } catch (_: Exception) {
            columns = emptyList()
            rows = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tableName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showQueryDialog = true }) {
                        Icon(Icons.Default.Code, contentDescription = "SQL Query")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (columns.isEmpty() && rows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bảng trống hoặc không thể đọc dữ liệu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Custom query result
                if (customQueryResult != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Kết quả truy vấn:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = customQueryResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Column headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    columns.forEach { column ->
                        Text(
                            text = column,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                HorizontalDivider()

                // Data rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            row.forEach { value ->
                                Text(
                                    text = value?.toString() ?: "NULL",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showQueryDialog) {
        AlertDialog(
            onDismissRequest = { showQueryDialog = false },
            title = { Text("SQL Query") },
            text = {
                Column {
                    Text(
                        text = "Nhập câu truy vấn SQL:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = { Text("SELECT * FROM ...") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            scope.launch {
                                try {
                                    val cmd = "sqlite3 '$databasePath' -separator '|' \"${queryText.replace("\"", "\\\"")}\""
                                    val result = RootUtils.executeCommand(cmd)
                                    customQueryResult = result.joinToString("\n").ifBlank { "Truy vấn thành công (không có kết quả)" }

                                    // If it's a SELECT, try to update the table view
                                    if (queryText.trim().startsWith("SELECT", ignoreCase = true)) {
                                        rows = parseSqliteData(result.joinToString("\n"), columns.size)
                                    } else {
                                        // Re-load data after modification
                                        val selectQuery = "sqlite3 '$databasePath' -separator '|' \"SELECT * FROM $tableName LIMIT 500\""
                                        val data = RootUtils.executeCommand(selectQuery).joinToString("\n")
                                        rows = parseSqliteData(data, columns.size)
                                    }
                                } catch (e: Exception) {
                                    customQueryResult = "Lỗi: ${e.message}"
                                }
                            }
                            showQueryDialog = false
                        }
                    }
                ) {
                    Text("Thực thi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQueryDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

data class TableInfo(
    val name: String,
    val columns: List<String>,
    val rowCount: Int
)

suspend fun loadTables(databasePath: String): List<TableInfo> {
    val tables = mutableListOf<TableInfo>()

    try {
        val listTablesCmd = "sqlite3 '$databasePath' \".tables\""
        val tablesList = RootUtils.executeCommand(listTablesCmd).joinToString("\n")

        if (tablesList.isNotBlank()) {
            tablesList.trim().split(Regex("\\s+")).forEach { tableName ->
                if (tableName.isNotBlank()) {
                    val countCmd = "sqlite3 '$databasePath' \"SELECT COUNT(*) FROM $tableName\""
                    val countStr = RootUtils.executeCommand(countCmd).firstOrNull()?.trim().orEmpty()
                    val rowCount = countStr.toIntOrNull() ?: 0

                    val schemaCmd = "sqlite3 '$databasePath' \".schema $tableName\""
                    val schema = RootUtils.executeCommand(schemaCmd).joinToString("\n")
                    val columns = getColumnsFromSchema(schema)

                    tables.add(TableInfo(tableName, columns, rowCount))
                }
            }
        }
    } catch (_: Exception) {
        return emptyList()
    }

    return tables
}

fun getColumnsFromSchema(schema: String): List<String> {
    val columns = mutableListOf<String>()
    val createMatch = Regex("""CREATE\s+TABLE\s+[`\"]?\w+[`\"]?\s*\(([^)]+)\)""", RegexOption.IGNORE_CASE)
    val match = createMatch.find(schema)

    if (match != null) {
        val columnsDef = match.groupValues[1]
        columnsDef.split(",").forEach { col ->
            val colName = col.trim().split(Regex("\\s+")).firstOrNull()?.trim('`', '"') ?: ""
            if (colName.isNotBlank() &&
                !colName.startsWith("PRIMARY", ignoreCase = true) &&
                !colName.startsWith("FOREIGN", ignoreCase = true) &&
                !colName.startsWith("UNIQUE", ignoreCase = true) &&
                !colName.startsWith("CHECK", ignoreCase = true) &&
                !colName.startsWith("CONSTRAINT", ignoreCase = true)
            ) {
                columns.add(colName)
            }
        }
    }

    return columns
}

fun parseSqliteData(data: String, columnCount: Int): List<List<Any?>> {
    if (columnCount <= 0) return emptyList()

    val rows = mutableListOf<List<Any?>>()
    data.lines().forEach { line ->
        if (line.isNotBlank()) {
            val values = line.split("|").map { it.trim() }
            if (values.size == columnCount) {
                rows.add(
                    values.map {
                        when {
                            it.isEmpty() || it == "NULL" -> null
                            it.toIntOrNull() != null -> it.toInt()
                            it.toDoubleOrNull() != null -> it.toDouble()
                            else -> it
                        }
                    }
                )
            }
        }
    }
    return rows
}
