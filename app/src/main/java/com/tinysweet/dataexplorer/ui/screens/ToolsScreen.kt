package com.tinysweet.dataexplorer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.ui.utils.AppIcons

/**
 * ToolsScreen - Công cụ nâng cao
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier,
    onToolClick: (toolId: String) -> Unit = {}
) {
    val toolsList = listOf(
        ToolItem(
            id = "sqlite",
            title = "SQLite Explorer",
            description = "Khám phá và chỉnh sửa cơ sở dữ liệu SQLite. Chọn app từ danh sách để bắt đầu.",
            icon = AppIcons.Database
        ),
        ToolItem(
            id = "sharedprefs",
            title = "SharedPreferences Editor",
            description = "Chỉnh sửa file cấu hình SharedPreferences. Chọn app từ danh sách để bắt đầu.",
            icon = AppIcons.Settings
        ),
        ToolItem(
            id = "backup",
            title = "Backup & Restore",
            description = "Sao lưu và khôi phục dữ liệu ứng dụng. Chọn app từ danh sách để bắt đầu.",
            icon = AppIcons.Backup
        ),
        ToolItem(
            id = "filebrowser",
            title = "Root File Browser",
            description = "Duyệt toàn bộ hệ thống file với quyền root",
            icon = AppIcons.FolderIcon
        )
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Công cụ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxWidth())

        Text(
            text = "Chọn một công cụ bên dưới. Các công cụ dữ liệu sẽ chuyển bạn sang tab Apps để chọn ứng dụng.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Tools list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(toolsList) { tool ->
                ToolCard(tool = tool, onClick = { onToolClick(tool.id) })
            }
        }
    }
}

@Composable
fun ToolCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.description,
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

data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)
