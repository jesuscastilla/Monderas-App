package com.lebeche.monderas.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.mail.ImapIdleService
import com.lebeche.monderas.app.ui.LoginScreen
import com.lebeche.monderas.app.ui.MonderasApp
import com.lebeche.monderas.app.ui.theme.MonderasTheme

class MainActivity : ComponentActivity() {

    private val permisoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.init(this)
        requestInitialPermissions()
        setContent {
            MonderasTheme {
                val loggedIn by SessionManager.isLoggedIn.collectAsState()

                LaunchedEffect(loggedIn) {
                    if (loggedIn) {
                        ImapIdleService.start(this@MainActivity)
                    } else {
                        ImapIdleService.stop(this@MainActivity)
                    }
                }

                if (loggedIn) {
                    MonderasApp(onLogout = { SessionManager.logout(this) })
                } else {
                    LoginScreen()
                }
            }
        }
    }

    private fun requestInitialPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.READ_CALENDAR)
        }
        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.WRITE_CALENDAR)
        }
        if (perms.isNotEmpty()) permisoLauncher.launch(perms.toTypedArray())
    }
}
