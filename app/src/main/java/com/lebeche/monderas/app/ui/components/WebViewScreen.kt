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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Credenciales para auto-login en un formulario web.
 * Campos por defecto: input[name=usuario] / input[name=clave] (panel STAFF).
 * Roundcube usa input[name=_user] / input[name=_pass], así que se pueden sobrescribir.
 */
data class AutoLogin(
    val usuario: String,
    val contrasena: String,
    val campoUsuario: String = "usuario",
    val campoClave: String = "clave",
)

/** Pantalla WebView reutilizable: carga una URL dentro de la app, con retroceso y spinner. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String, 
    autoLogin: AutoLogin? = null, 
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (WebView?) -> Unit = {}
) {
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
                    // UA móvil para que Roundcube/DSM sirvan el layout móvil (no el de escritorio).
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

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
                            val interno = host.endsWith("corrientelebeche.es") || host.endsWith("synology.me")
                            if (scheme == "mailto" ||
                                (scheme in listOf("http", "https") &&
                                    host.isNotEmpty() &&
                                    !interno)
                            ) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, destino))
                                return true
                            }
                            return false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?): Boolean {
                            if (message != null) {
                                android.widget.Toast.makeText(ctx, message, android.widget.Toast.LENGTH_SHORT).show()
                            }
                            result?.confirm()
                            return true
                        }
                    }
                    loadUrl(url)
                }.also { webViewState.value = it }
            },
        )

        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Box(Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            floatingActionButton(webViewState.value)
        }
    }
}

private fun buildAutoLoginJs(creds: AutoLogin): String {
    val usuario = creds.usuario.jsEscape()
    val contrasena = creds.contrasena.jsEscape()
    return """(function(){var u=document.querySelector('input[name="${creds.campoUsuario}"]');var c=document.querySelector('input[name="${creds.campoClave}"]');if(u&&c){u.value='$usuario';c.value='$contrasena';try{u.dispatchEvent(new Event('input',{bubbles:true}));c.dispatchEvent(new Event('input',{bubbles:true}));}catch(e){}var b=document.querySelector('button[type="submit"],input[type="submit"]');if(b){b.click();}else{var f=document.querySelector('form');if(f){f.submit();}}}})();"""
}

private fun String.jsEscape(): String =
    replace("\\", "\\\\").replace("'", "\\'")
