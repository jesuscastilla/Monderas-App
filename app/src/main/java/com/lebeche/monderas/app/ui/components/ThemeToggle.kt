package com.lebeche.monderas.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.lebeche.monderas.app.data.SessionManager

@Composable
fun ThemeToggle() {
    val context = LocalContext.current
    val isDark by SessionManager.isDarkMode.collectAsState()

    // Si es nulo (System), determinamos por defecto que sea claro para que el click lo ponga oscuro,
    // o calculamos el valor real si queremos un toggle preciso.
    val isSystemDark = isSystemInDarkTheme()
    val currentlyDark = isDark ?: isSystemDark

    IconButton(
        onClick = { SessionManager.setDarkMode(context, !currentlyDark) }
    ) {
        Icon(
            imageVector = if (currentlyDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = "Cambiar tema"
        )
    }
}
