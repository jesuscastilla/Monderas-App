package com.lebeche.monderas.app.calendario.ui

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebeche.monderas.app.calendario.Repository
import com.lebeche.monderas.app.data.SessionManager
import kotlinx.coroutines.launch

import java.time.LocalDate

/** Usuario CalDAV de la asociación (cuenta compartida de las Monderas). */
private const val CALDAV_USER = "lebeche"

sealed class Screen {
    object Main : Screen()
    data class Detail(val eventId: Long) : Screen()
    data class Edit(val eventId: Long?, val defaultDate: LocalDate? = null) : Screen()
    object Settings : Screen()
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository.get(app)
    var initialized by mutableStateOf(value = false)
    var hasAccounts by mutableStateOf(value = false)

    init {
        viewModelScope.launch {
            hasAccounts = repo.accounts().isNotEmpty()
            if (!hasAccounts) {
                // Auto-login con la cuenta compartida de las Monderas.
                val pass = SessionManager.contrasena(app)
                if (pass.isNotEmpty()) {
                    runCatching {
                        repo.addAccount(
                            name = Repository.DEFAULT_ACCOUNT_NAME,
                            baseUrl = Repository.DEFAULT_CALDAV_URL,
                            username = CALDAV_USER,
                            password = pass,
                            insecureTls = false,
                        )
                    }
                    hasAccounts = repo.accounts().isNotEmpty()
                }
            }
            initialized = true
        }
    }

    fun refresh() {
        viewModelScope.launch {
            hasAccounts = repo.accounts().isNotEmpty()
        }
    }
}

/** Raíz de la app: gestiona la navegación simple entre pantallas. */
@Composable
fun CalendarApp() {
    val appVm: AppViewModel = viewModel()
    var screen by remember { mutableStateOf<Screen>(Screen.Main) }

    when {
        !appVm.initialized -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        !appVm.hasAccounts -> WelcomeScreen(onConnected = { appVm.refresh() })

        else -> {
            BackHandler(enabled = screen != Screen.Main) {
                screen = Screen.Main
            }
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    (fadeIn(tween(240)) togetherWith fadeOut(tween(140)))
                },
                label = "pantalla",
            ) { s ->
                when (s) {
                    is Screen.Main -> MainScreen(
                        onOpenEvent = { screen = Screen.Detail(it) },
                        onCreateEvent = { date -> screen = Screen.Edit(null, date) },
                        onOpenSettings = { screen = Screen.Settings }
                    )
                    is Screen.Detail -> EventDetailScreen(
                        eventId = s.eventId,
                        onBack = { screen = Screen.Main },
                        onEdit = { screen = Screen.Edit(s.eventId) }
                    )
                    is Screen.Edit -> EventEditScreen(
                        eventId = s.eventId,
                        defaultDate = s.defaultDate,
                        onDone = { screen = Screen.Main }
                    )
                    is Screen.Settings -> SettingsScreen(
                        onBack = { screen = Screen.Main }
                    )
                }
            }
        }
    }
}