package com.lebeche.monderas.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lebeche.monderas.app.ui.screens.AdminScreen
import com.lebeche.monderas.app.ui.screens.CalendarioScreen
import com.lebeche.monderas.app.ui.screens.CorreoScreen

/** Pestañas principales de la app Monderas. */
enum class MonderasTab(val etiqueta: String, val icono: ImageVector) {
    Calendario("Calendario", Icons.Filled.CalendarMonth),
    Correo("Correo", Icons.Filled.Email),
    Admin("Admin", Icons.Filled.AdminPanelSettings),
}

@Composable
fun MonderasApp() {
    var pestana by rememberSaveable { mutableStateOf(MonderasTab.Calendario) }

    Scaffold(
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
                MonderasTab.Admin -> AdminScreen()
            }
        }
    }
}
