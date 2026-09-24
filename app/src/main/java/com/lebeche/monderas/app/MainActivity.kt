package com.lebeche.monderas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lebeche.monderas.app.data.SessionManager
import com.lebeche.monderas.app.ui.LoginScreen
import com.lebeche.monderas.app.ui.MonderasApp
import com.lebeche.monderas.app.ui.theme.MonderasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.init(this)
        setContent {
            MonderasTheme {
                val loggedIn by SessionManager.isLoggedIn.collectAsState()
                if (loggedIn) {
                    MonderasApp(onLogout = { SessionManager.logout(this) })
                } else {
                    LoginScreen()
                }
            }
        }
    }
}
