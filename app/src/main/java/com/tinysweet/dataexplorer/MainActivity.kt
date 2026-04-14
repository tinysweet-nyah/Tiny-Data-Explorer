package com.tinysweet.dataexplorer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.tinysweet.dataexplorer.ui.theme.TinyDataExplorerTheme
import com.tinysweet.dataexplorer.ui.components.LoadingScreen
import com.tinysweet.dataexplorer.ui.components.RootRequestScreen
import com.tinysweet.dataexplorer.ui.screens.MainScreen
import com.tinysweet.dataexplorer.utils.RootUtils

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TinyDataExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        RootUtils.closeShell()
    }
}

@Composable
fun AppContent() {
    var isRootChecked by remember { mutableStateOf(false) }
    var isRooted by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isChecking = true
        isRooted = RootUtils.isRootAvailable()
        isChecking = false
        isRootChecked = true
    }

    when {
        isChecking -> {
            LoadingScreen(message = "Đang kiểm tra quyền root...")
        }
        !isRootChecked || !isRooted -> {
            RootRequestScreen(
                onRequestRoot = {
                    scope.launch {
                        isRooted = RootUtils.isRootAvailable()
                        if (!isRooted) {
                            Toast.makeText(
                                context,
                                "Cần quyền root để sử dụng app này!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }
        else -> {
            MainScreen()
        }
    }
}