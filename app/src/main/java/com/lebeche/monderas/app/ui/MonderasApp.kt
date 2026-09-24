package com.lebeche.monderas.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lebeche.monderas.app.ui.screens.CalendarioScreen
import com.lebeche.monderas.app.ui.screens.CorreoScreen
import com.lebeche.monderas.app.ui.screens.WebScreen

/** Pestañas principales de la app Monderas. */
enum class MonderasTab(val etiqueta: String, val icono: ImageVector) {
    Calendario("Calendario", Icons.Filled.CalendarMonth),
    Correo("Correo", Icons.Filled.Email),
    Web("Web", Icons.Filled.Language),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonderasApp(onLogout: () -> Unit) {
    var pestana by rememberSaveable { mutableStateOf(MonderasTab.Calendario) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monderas") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MonderasTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = pestana == tab,
                        onClick = { pestana = tab },
                        icon = { Icon(tab.icono, contentDescription = tab.etiqueta) },
                        label = { Text(tab.etiqueta) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (pestana) {
                MonderasTab.Calendario -> CalendarioScreen()
                MonderasTab.Correo -> CorreoScreen()
                MonderasTab.Web -> WebScreen()
            }
        }
    }
}
