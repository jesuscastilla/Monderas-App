package com.lebeche.monderas.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.ui.WebUrls
import com.lebeche.monderas.app.ui.components.AutoLogin
import com.lebeche.monderas.app.ui.components.WebViewScreen

@Composable
fun WebScreen() {
    val context = LocalContext.current
    val contrasena = remember { SessionManager.contrasena(context) }
    WebViewScreen(
        url = WebUrls.WEB,
        autoLogin = AutoLogin(usuario = "lebeche", contrasena = contrasena),
    )
}
