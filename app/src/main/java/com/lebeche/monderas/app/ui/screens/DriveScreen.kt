package com.lebeche.monderas.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.ui.WebUrls
import com.lebeche.monderas.app.ui.components.WebViewScreen
import com.lebeche.monderas.app.ui.components.buildSynologyLoginJs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveScreen() {
    val context = LocalContext.current
    val contrasena = remember { SessionManager.contrasena(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Drive") }) }
    ) { padding ->
        WebViewScreen(
            modifier = Modifier.fillMaxSize().padding(padding),
            url = WebUrls.DRIVE,
            userAgent = WebUrls.DRIVE_UA,
            scriptAutoLogin = buildSynologyLoginJs(
                account = WebUrls.DRIVE_USER,
                password = contrasena,
                launchUrl = WebUrls.DRIVE,
            ),
        )
    }
}
