package com.lebeche.monderas.app.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.lebeche.monderas.app.data.SessionManager

private enum class UnauthTab(val label: String, val icon: ImageVector) {
    Login("Acceder", Icons.Filled.Person),
    Info("Info", Icons.Filled.Info)
}

/** Pantalla de inicio de sesión de la app. */
@Composable
fun LoginScreen() {
    var unauthTab by remember { mutableStateOf(UnauthTab.Login) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                UnauthTab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = unauthTab == t,
                        onClick = { unauthTab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .imePadding()
        ) {
            when (unauthTab) {
                UnauthTab.Login -> LoginContent()
                UnauthTab.Info -> InfoContent()
            }
        }
    }
}

enum class LoginErrorState {
    NONE, BAD_CREDENTIALS, NETWORK_ERROR
}

@Composable
private fun LoginContent() {
    var usuario by remember { mutableStateOf(SessionManager.USUARIO) }
    var contrasena by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf(LoginErrorState.NONE) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Mondera's App", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Gestión interna de la asociación",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = usuario,
            onValueChange = { 
                usuario = it
                errorState = LoginErrorState.NONE
            },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = contrasena,
            onValueChange = { 
                contrasena = it
                errorState = LoginErrorState.NONE
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        
        if (errorState != LoginErrorState.NONE) {
            Spacer(Modifier.height(12.dp))
            val errorMessage = when (errorState) {
                LoginErrorState.NETWORK_ERROR -> "No hay conexión a internet o el servidor no responde"
                LoginErrorState.BAD_CREDENTIALS -> "Usuario o contraseña incorrectos"
                else -> ""
            }
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (!isNetworkAvailable(context)) {
                    errorState = LoginErrorState.NETWORK_ERROR
                } else if (!SessionManager.login(context, usuario, contrasena)) {
                    errorState = LoginErrorState.BAD_CREDENTIALS
                } else {
                    errorState = LoginErrorState.NONE
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Entrar")
        }
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
private fun InfoContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Información", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            buildAnnotatedString {
                append("Esta aplicación es de uso exclusivo para las ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Monderas")
                }
                append(", las organizadoras del Colectivo Lebeche.\n\n")
                append("Proporciona acceso unificado a las herramientas de gestión interna:\n\n")
                append("• Calendario interno\n")
                append("• Correo corporativo\n")
                append("• Panel Web (Staff)\n\n")
                append("Si tienes alguna duda o algo no funciona correctamente, ponte en contacto con la ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Comisión de Digitalización")
                }
                append(" del colectivo.")
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
