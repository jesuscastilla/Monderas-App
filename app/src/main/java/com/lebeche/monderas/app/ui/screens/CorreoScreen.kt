package com.lebeche.monderas.app.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.mail.MailConfig
import com.lebeche.monderas.app.mail.MailDetail
import com.lebeche.monderas.app.mail.MailRepository
import com.lebeche.monderas.app.mail.MailSummary
import kotlinx.coroutines.launch

class CorreoViewModel(app: Application) : AndroidViewModel(app) {
    var cargando by mutableStateOf(false)
    var correos by mutableStateOf<List<MailSummary>>(emptyList())
    var detalle by mutableStateOf<MailDetail?>(null)
    var error by mutableStateOf<String?>(null)

    fun cargar() {
        val pass = SessionManager.contrasena(getApplication())
        if (pass.isEmpty()) {
            error = "No hay sesión"
            return
        }
        cargando = true
        viewModelScope.launch {
            try {
                correos = MailRepository.fetchInbox(MailConfig.EMAIL, pass)
                error = null
            } catch (e: Exception) {
                error = "No se pudo conectar al correo: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    fun abrir(uid: Long) {
        val pass = SessionManager.contrasena(getApplication())
        viewModelScope.launch {
            try {
                detalle = MailRepository.fetchDetail(MailConfig.EMAIL, pass, uid)
            } catch (e: Exception) {
                error = "No se pudo leer el correo"
            }
        }
    }

    fun cerrarDetalle() {
        detalle = null
    }
}

@Composable
fun CorreoScreen() {
    val vm: CorreoViewModel = viewModel()
    LaunchedEffect(Unit) { vm.cargar() }

    val d = vm.detalle
    if (d != null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(d.from, style = MaterialTheme.typography.titleMedium)
            Text(d.subject, style = MaterialTheme.typography.titleLarge)
            Text(
                d.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(d.body, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { vm.cerrarDetalle() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Volver")
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedButton(onClick = { vm.cargar() }, modifier = Modifier.padding(8.dp)) {
            Text("Recargar")
        }
        when {
            vm.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            vm.error != null -> Text(
                vm.error.orEmpty(),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
            vm.correos.isEmpty() -> Text("No hay correos", modifier = Modifier.padding(16.dp))
            else -> LazyColumn {
                items(vm.correos, key = { it.uid }) { c ->
                    ListItem(
                        headlineContent = {
                            Text(
                                if (c.from.isBlank()) "(sin remitente)" else c.from,
                                style = if (c.seen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            )
                        },
                        supportingContent = { Text(c.subject) },
                        overlineContent = { Text(c.date) },
                        modifier = Modifier.clickable { vm.abrir(c.uid) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
