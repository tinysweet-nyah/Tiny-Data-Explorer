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
import com.tinysweet.dataexplorer.ui.utils.AppIcons

/**
 * Navigation destinations
 */
sealed class NavScreen {
    object Apps : NavScreen()
    object FileBrowser : NavScreen()
    object Tools : NavScreen()
    data class AppDetail(val packageName: String, val appName: String) : NavScreen()
    data class SQLiteExplorer(val packageName: String) : NavScreen()
    data class DatabaseTables(val dbPath: String, val dbName: String) : NavScreen()
    data class SharedPrefsEditor(val packageName: String) : NavScreen()
    data class SharedPrefsDetail(val filePath: String, val fileName: String) : NavScreen()
    data class BackupRestore(val packageName: String) : NavScreen()
    data class AppFileBrowser(val packageName: String) : NavScreen()
}

@Composable
fun MainScreen() {
    val currentScreen = remember { mutableStateOf<NavScreen>(NavScreen.Apps) }
    val tabState = remember { mutableStateOf(0) }

    // Check if we're on a sub-screen (not a top-level tab)
    val isSubScreen = currentScreen.value !is NavScreen.Apps &&
        currentScreen.value !is NavScreen.FileBrowser &&
        currentScreen.value !is NavScreen.Tools

    if (isSubScreen) {
        // Show sub-screen without bottom nav
        SubScreenContent(currentScreen)
        return
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(tabState, currentScreen)
        }
    ) { padding ->
        when (tabState.value) {
            0 -> AppsScreen(
                modifier = Modifier.padding(padding),
                onAppClick = { packageName, appName ->
                    currentScreen.value = NavScreen.AppDetail(packageName, appName)
                }
            )
            1 -> FileBrowserScreen(modifier = Modifier.padding(padding))
            2 -> ToolsScreen(
                modifier = Modifier.padding(padding),
                onToolClick = { toolId ->
                    when (toolId) {
                        "sqlite" -> {
                            // Will show app picker first
                            currentScreen.value = NavScreen.Apps
                            tabState.value = 0
                        }
                        "sharedprefs" -> {
                            currentScreen.value = NavScreen.Apps
                            tabState.value = 0
                        }
                        "backup" -> {
                            currentScreen.value = NavScreen.Apps
                            tabState.value = 0
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SubScreenContent(currentScreen: MutableState<NavScreen>) {
    when (val screen = currentScreen.value) {
        is NavScreen.AppDetail -> AppDetailScreen(
            packageName = screen.packageName,
            appName = screen.appName,
            onBack = { currentScreen.value = NavScreen.Apps },
            onNavigateToSQLite = { pkg ->
                currentScreen.value = NavScreen.SQLiteExplorer(pkg)
            },
            onNavigateToSharedPrefs = { pkg ->
                currentScreen.value = NavScreen.SharedPrefsEditor(pkg)
            },
            onNavigateToBackup = { pkg ->
                currentScreen.value = NavScreen.BackupRestore(pkg)
            },
            onNavigateToFileBrowser = { pkg ->
                currentScreen.value = NavScreen.AppFileBrowser(pkg)
            }
        )
        is NavScreen.SQLiteExplorer -> SQLiteExplorerScreen(
            appPackageName = screen.packageName,
            onBack = { currentScreen.value = NavScreen.Apps },
            onDatabaseClick = { dbPath, dbName ->
                currentScreen.value = NavScreen.DatabaseTables(dbPath, dbName)
            }
        )
        is NavScreen.DatabaseTables -> DatabaseTablesScreen(
            databasePath = screen.dbPath,
            databaseName = screen.dbName,
            onBack = {
                // We don't know the package name easily, go back to Apps
                currentScreen.value = NavScreen.Apps
            }
        )
        is NavScreen.SharedPrefsEditor -> SharedPreferencesEditorScreen(
            appPackageName = screen.packageName,
            onBack = { currentScreen.value = NavScreen.Apps },
            onPrefsFileClick = { filePath, fileName ->
                currentScreen.value = NavScreen.SharedPrefsDetail(filePath, fileName)
            }
        )
        is NavScreen.SharedPrefsDetail -> SharedPreferencesDetailScreen(
            prefsFilePath = screen.filePath,
            prefsFileName = screen.fileName,
            onBack = { currentScreen.value = NavScreen.Apps }
        )
        is NavScreen.BackupRestore -> BackupRestoreScreen(
            appPackageName = screen.packageName,
            onBack = { currentScreen.value = NavScreen.Apps }
        )
        is NavScreen.AppFileBrowser -> FileBrowserScreen(
            initialPath = "/data/data/${screen.packageName}"
        )
        else -> { /* handled by tab content */ }
    }
}

@Composable
fun BottomNavigation(tabState: MutableState<Int>, currentScreen: MutableState<NavScreen>) {
    NavigationBar {
        NavigationBarItem(
            selected = tabState.value == 0,
            onClick = {
                tabState.value = 0
                currentScreen.value = NavScreen.Apps
            },
            icon = { androidx.compose.material3.Icon(AppIcons.AppIcon, contentDescription = null) },
            label = { Text(text = "Apps") }
        )

        NavigationBarItem(
            selected = tabState.value == 1,
            onClick = {
                tabState.value = 1
                currentScreen.value = NavScreen.FileBrowser
            },
            icon = { androidx.compose.material3.Icon(AppIcons.FolderIcon, contentDescription = null) },
            label = { Text(text = "File Browser") }
        )

        NavigationBarItem(
            selected = tabState.value == 2,
            onClick = {
                tabState.value = 2
                currentScreen.value = NavScreen.Tools
            },
            icon = { androidx.compose.material3.Icon(AppIcons.ToolsIcon, contentDescription = null) },
            label = { Text(text = "Tools") }
        )
    }
}
