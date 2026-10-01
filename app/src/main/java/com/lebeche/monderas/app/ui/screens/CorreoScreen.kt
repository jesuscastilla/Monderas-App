package com.lebeche.monderas.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lebeche.monderas.app.data.SessionManager
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.lebeche.monderas.app.ui.components.ThemeToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorreoScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val contrasena = remember { SessionManager.contrasena(context) }
    val usuario = "monderas@corrientelebeche.es"
    var showTutorial by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Correo") },
                actions = { ThemeToggle() }
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Info, 
                        contentDescription = "Info", 
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Configura el correo en tu aplicación favorita (Gmail, Outlook, correo de iOS...) para recibir notificaciones en tiempo real y poder responder mensajes cómodamente.",
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text("Tus datos de acceso", style = MaterialTheme.typography.titleLarge)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ConfigRow("Usuario", usuario) {
                        clipboard.setText(AnnotatedString(usuario))
                    }
                    ConfigRow("Contraseña", contrasena) {
                        clipboard.setText(AnnotatedString(contrasena))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Servidor de Entrada (IMAP)", style = MaterialTheme.typography.titleMedium)
                    Text("• Servidor: imap.dominioabsoluto.net", style = MaterialTheme.typography.bodyMedium)
                    Text("• Puerto: 993 (SSL/TLS)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Servidor de Salida (SMTP)", style = MaterialTheme.typography.titleMedium)
                    Text("• Servidor: smtp.dominioabsoluto.net", style = MaterialTheme.typography.bodyMedium)
                    Text("• Puerto: 587 (STARTTLS)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ Usa dominioabsoluto.net y NO corrientelebeche.es para los servidores.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { showTutorial = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver tutorial paso a paso", modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
            }

            Button(
                onClick = {
                    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "No se encontró ninguna aplicación de correo instalada.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Email, contentDescription = null)
                Text("Abrir mi aplicación de Correo", modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 12.dp))
            }
        }
        
        if (showTutorial) {
            AlertDialog(
                onDismissRequest = { showTutorial = false },
                title = { Text("Configurar en Gmail") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Abre Gmail y ve a Ajustes > Añadir cuenta.")
                        Text("2. Selecciona Otra.")
                        Text("3. Escribe monderas@corrientelebeche.es y dale a Siguiente.")
                        Text("4. Elige Personal (IMAP).")
                        Text("5. Escribe la Contraseña del buzón.")
                        Text("6. En Servidor de entrada, pon imap.dominioabsoluto.net (puerto 993).")
                        Text("7. En Servidor de salida, pon smtp.dominioabsoluto.net (puerto 587).")
                        Text("8. ¡Listo!")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTutorial = false }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfigRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = {
                onCopy()
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
