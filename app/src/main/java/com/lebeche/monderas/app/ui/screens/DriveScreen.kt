package com.lebeche.monderas.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.drive.DriveItem
import com.lebeche.monderas.app.drive.DriveRepository
import com.lebeche.monderas.app.ui.components.ThemeToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveScreen() {
    val context = LocalContext.current
    val contrasena = remember { SessionManager.contrasena(context) }
    val repo = remember { DriveRepository() }
    val scope = rememberCoroutineScope()

    var cargando by remember { mutableStateOf(true) }
    var accionEnProgreso by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<DriveItem>>(emptyList()) }
    val pathStack = remember { mutableStateListOf<String>("/home") } // "/home" es la raíz por defecto para la cuenta

    fun refrescarCarpeta() {
        scope.launch {
            cargando = true
            val isLogged = repo.login(contrasena)
            if (isLogged) {
                items = repo.listFiles(pathStack.last())
                error = null
            } else {
                error = "No se pudo conectar a Synology Drive"
            }
            cargando = false
        }
    }

    LaunchedEffect(pathStack.size) {
        refrescarCarpeta()
    }

    // Selector para SUBIR un archivo
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                accionEnProgreso = true
                val currentPath = pathStack.last()
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                var fileName = "archivo_subido"
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = it.getString(nameIndex)
                    }
                }
                
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    if (stream != null) {
                        val success = repo.uploadFile(currentPath, fileName, stream)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(context, "Archivo subido con éxito", Toast.LENGTH_SHORT).show()
                                refrescarCarpeta()
                            } else {
                                Toast.makeText(context, "Error al subir el archivo", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error de I/O al subir", Toast.LENGTH_LONG).show()
                    }
                }
                accionEnProgreso = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pathStack.size > 1) pathStack.last().substringAfterLast("/") else "Drive") },
                navigationIcon = {
                    if (pathStack.size > 1) {
                        IconButton(onClick = { pathStack.removeAt(pathStack.lastIndex) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                },
                actions = { ThemeToggle() }
            )
        },
        floatingActionButton = {
            if (!cargando && error == null && !accionEnProgreso) {
                FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Filled.CloudUpload, "Subir archivo")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                cargando || accionEnProgreso -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        if (accionEnProgreso) {
                            Text("Procesando archivo...", modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                items.isEmpty() -> {
                    Text("Carpeta vacía", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(items, key = { it.path }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.isDir) {
                                            pathStack.add(item.path)
                                        } else {
                                            // DESCARGAR archivo
                                            scope.launch {
                                                accionEnProgreso = true
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse("https://pelotxo.synology.me:5001/webapi/entry.cgi?api=SYNO.FileStation.Download&version=2&method=download&path=${Uri.encode(item.path)}")
                                                try {
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "No se pudo iniciar la descarga", Toast.LENGTH_SHORT).show()
                                                }
                                                accionEnProgreso = false
                                            }
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isDir) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (item.isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!item.isDir) {
                                    Icon(
                                        imageVector = Icons.Filled.Download,
                                        contentDescription = "Descargar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
