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
import com.lebeche.monderas.app.ui.components.AutoLogin
import com.lebeche.monderas.app.ui.components.WebViewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorreoScreen() {
    val context = LocalContext.current
    val contrasena = remember { SessionManager.contrasena(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Correo") }) }
    ) { padding ->
        WebViewScreen(
            modifier = Modifier.fillMaxSize().padding(padding),
            url = WebUrls.ROUNDCUBE,
            autoLogin = AutoLogin(
                usuario = WebUrls.ROUNDCUBE_USER,
                contrasena = contrasena,
                campoUsuario = "_user",
                campoClave = "_pass",
            ),
        )
    }
}
