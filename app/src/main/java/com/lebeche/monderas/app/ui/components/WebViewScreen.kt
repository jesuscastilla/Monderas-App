package com.lebeche.monderas.app.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/** Credenciales para auto-login en un formulario web (input[name=usuario] / input[name=clave]). */
data class AutoLogin(val usuario: String, val contrasena: String)

/** Pantalla WebView reutilizable: carga una URL dentro de la app, con retroceso y spinner. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, autoLogin: AutoLogin? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    var cargando by remember { mutableStateOf(true) }

    BackHandler(enabled = webViewState.value?.canGoBack() == true) {
        webViewState.value?.goBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            cargando = false
                            autoLogin?.let { creds ->
                                view?.evaluateJavascript(buildAutoLoginJs(creds), null)
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val destino = request?.url ?: return false
                            val scheme = destino.scheme.orEmpty()
                            val host = destino.host.orEmpty()
                            if (scheme == "mailto" ||
                                (scheme in listOf("http", "https") &&
                                    host.isNotEmpty() &&
                                    !host.endsWith("corrientelebeche.es"))
                            ) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, destino))
                                return true
                            }
                            return false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }.also { webViewState.value = it }
            },
        )

        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private fun buildAutoLoginJs(creds: AutoLogin): String =
    """(function(){var u=document.querySelector('input[name="usuario"]');var c=document.querySelector('input[name="clave"]');if(u&&c){u.value='${creds.usuario}';c.value='${creds.contrasena}';var f=document.querySelector('form');if(f){f.submit();}}})();"""
