package com.lebeche.monderas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lebeche.monderas.app.ui.MonderasApp
import com.lebeche.monderas.app.ui.theme.MonderasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonderasTheme {
                MonderasApp()
            }
        }
    }
}
