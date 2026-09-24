package com.lebeche.monderas.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AzulSombra,
    onPrimary = Color.White,
    primaryContainer = AzulTinte,
    onPrimaryContainer = AzulMasOscuro,
    secondary = Ambar,
    onSecondary = Tinta,
    background = Papel,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
)

private val DarkColors = darkColorScheme(
    primary = AzulMadre,
    onPrimary = AzulMasOscuro,
    primaryContainer = AzulSombraOscura,
    onPrimaryContainer = AzulTinte,
    secondary = Ambar,
    onSecondary = Tinta,
    background = AzulMasOscuro,
    onBackground = Color.White,
    surface = AzulSombraOscura,
    onSurface = Color.White,
)

@Composable
fun MonderasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
