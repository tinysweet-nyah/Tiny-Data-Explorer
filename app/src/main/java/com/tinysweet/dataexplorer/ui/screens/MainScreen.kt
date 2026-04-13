package com.tinysweet.dataexplorer.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tinysweet.dataexplorer.ui.theme.RootForgeDataExplorerTheme
import com.tinysweet.dataexplorer.ui.utils.AppIcons

@Composable
fun MainScreen() {
    val tabState = remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigation(tabState)
        }
    ) { padding ->
        RootForgeDataExplorerTheme {
            when (tabState.value) {
                0 -> AppsScreen(modifier = Modifier.padding(padding))
                1 -> FileBrowserScreen(modifier = Modifier.padding(padding))
                2 -> ToolsScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun BottomNavigation(tabState: MutableState<Int>) {
    NavigationBar {
        NavigationBarItem(
            selected = tabState.value == 0,
            onClick = { tabState.value = 0 },
            icon = { androidx.compose.material3.Icon(AppIcons.AppIcon, contentDescription = null) },
            label = { Text(text = "Apps") }
        )
        
        NavigationBarItem(
            selected = tabState.value == 1,
            onClick = { tabState.value = 1 },
            icon = { androidx.compose.material3.Icon(AppIcons.FolderIcon, contentDescription = null) },
            label = { Text(text = "File Browser") }
        )
        
        NavigationBarItem(
            selected = tabState.value == 2,
            onClick = { tabState.value = 2 },
            icon = { androidx.compose.material3.Icon(AppIcons.ToolsIcon, contentDescription = null) },
            label = { Text(text = "Tools") }
        )
    }
}