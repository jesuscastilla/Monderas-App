package com.lebeche.monderas.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
fun WebScreen() {
    val context = LocalContext.current
    val contrasena = remember { SessionManager.contrasena(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Panel STAFF") })
        }
    ) { padding ->
        WebViewScreen(
            modifier = Modifier.fillMaxSize().padding(padding),
            url = WebUrls.WEB,
            autoLogin = AutoLogin(usuario = "lebeche", contrasena = contrasena),
            floatingActionButton = { webView ->
                ExtendedFloatingActionButton(
                    onClick = {
                        val js = "(function() { " +
                                "var panel = document.querySelector('.admin__panel.activo'); " +
                                "var btn = panel ? panel.querySelector('.btn--guardar') : document.querySelector('.btn--guardar'); " +
                                "if (btn) { btn.click(); } " +
                                "else { alert('No se ha encontrado el botón de guardar en esta pantalla.'); } " +
                                "})();"
                        webView?.evaluateJavascript(js, null)
                    },
                    icon = { Icon(Icons.Filled.Save, "Guardar") },
                    text = { Text("Guardar") }
                )
            }
        )
    }
}
