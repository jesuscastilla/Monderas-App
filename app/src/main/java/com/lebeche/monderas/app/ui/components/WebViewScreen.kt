package com.lebeche.monderas.app.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "WebViewScreen"

/** UA móvil por defecto (Roundcube sirve el layout móvil). Drive lo sobrescribe con un UA de escritorio. */
private const val UA_MOVIL =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

/**
 * El WebView de DSM/Roundcube se muestra como un rectángulo negro/blanco en algunos
 * dispositivos (bug de GPU/composición con hardware acceleration + Compose). Forzar
 * renderizado por software evita ese rectángulo y garantiza que el contenido se dibuje.
 */
private const val RENDERIZADO_SOFTWARE = true

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

/** Pantalla WebView reutilizable: carga una URL dentro de la app, con retroceso, spinner y errores visibles. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    autoLogin: AutoLogin? = null,
    scriptAutoLogin: String? = null,
    modifier: Modifier = Modifier,
    userAgent: String? = null,
    floatingActionButton: @Composable (WebView?) -> Unit = {}
) {
    val context = LocalContext.current
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var generacion by remember { mutableStateOf(0) }
    // URLs donde ya se intentó el auto-login (una sola vez por URL; evita bucles de reenvío).
    val yaIntentadas = remember { mutableSetOf<String>() }

    // Aceptar cookies (Cloudflare y tokens de DSM/Roundcube las necesitan).
    remember {
        CookieManager.getInstance().setAcceptCookie(true)
    }

    BackHandler(enabled = webViewState.value?.canGoBack() == true) {
        webViewState.value?.goBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        key(generacion) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
                        if (RENDERIZADO_SOFTWARE) {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.setSupportMultipleWindows(true)
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.userAgentString = userAgent ?: UA_MOVIL

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                cargando = false
                                // Auto-login una sola vez por URL (fin del bucle de reenvío).
                                autoLogin?.let { creds ->
                                    val u = url.orEmpty()
                                    if (!yaIntentadas.contains(u)) {
                                        yaIntentadas.add(u)
                                        view?.evaluateJavascript(buildAutoLoginJs(creds), null)
                                    }
                                }
                                // Auto-login por script (p. ej. API de Synology): una sola vez por URL.
                                scriptAutoLogin?.let { script ->
                                    val u = url.orEmpty()
                                    if (!yaIntentadas.contains(u)) {
                                        yaIntentadas.add(u)
                                        view?.evaluateJavascript(script, null)
                                    }
                                }
                                // Watchdog: si el body quedó vacío, lo convertimos en error visible.
                                view?.evaluateJavascript(
                                    "(function(){try{if(!document.body)return 0;var t=(document.body.innerText||'').trim().length;var c=document.body.childElementCount;return (t===0&&c===0)?0:1}catch(e){return -1}})()"
                                ) { valor ->
                                    val n = valor?.trim()?.toIntOrNull()
                                    if (n == 0 && error == null) {
                                        error = "La página se ha cargado vacía ($url)."
                                    }
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val destino = request?.url ?: return false
                                val scheme = destino.scheme.orEmpty()
                                val host = destino.host.orEmpty()
                                Log.d(TAG, "Navegando a: $destino")
                                val interno = host.endsWith("corrientelebeche.es") || host.endsWith("synology.me")
                                if (scheme == "mailto" ||
                                    (scheme in listOf("http", "https") &&
                                        host.isNotEmpty() &&
                                        !interno)
                                ) {
                                    Log.d(TAG, "Abriendo en navegador externo: $destino")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, destino))
                                    return true
                                }
                                return false
                            }

                            @SuppressLint("WebViewClientOnReceivedSslError")
                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?
                            ) {
                                // Siempre procedemos porque nuestras URLs internas tienen Cloudflare Origin o son autofirmados
                                handler?.proceed()
                            }
                        
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                webError: WebResourceError?
                            ) {
                                Log.e(TAG, "Error HTTP/Net: ${webError?.description} / ${webError?.errorCode} en ${request?.url}")
                                if (request?.isForMainFrame == true) {
                                    error = "No se pudo cargar la página (error de red ${webError?.errorCode})."
                                    cargando = false
                                }
                                super.onReceivedError(view, request, webError)
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?
                            ) {
                                Log.e(TAG, "HTTP ${errorResponse?.statusCode} en ${request?.url}")
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                Log.e(TAG, "Render process gone: crash=${detail?.didCrash()}")
                                webViewState.value = null
                                error = "El visor web se ha detenido (posible falta de memoria). Pulsa «Reintentar»."
                                cargando = false
                                return true
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                // window.open / target=_blank: abrir en la misma WebView (DSM lanza apps en ventana nueva).
                                resultMsg?.let { msg ->
                                    (msg.obj as? android.webkit.WebView.WebViewTransport)?.webView = view
                                    msg.sendToTarget()
                                    return true
                                }
                                return false
                            }

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
                update = { webView ->
                    if (webView.originalUrl != url && webView.url != url) {
                        cargando = true
                        error = null
                        webView.loadUrl(url)
                    }
                },
                onRelease = { it.destroy() }
            )
        }

        if (cargando && error == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        error?.let { msg ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("No se ha podido cargar la página", style = MaterialTheme.typography.titleMedium)
                Text(msg, style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = {
                        error = null
                        cargando = true
                        yaIntentadas.remove(url)
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                        generacion++
                    }
                ) {
                    Text("Reintentar")
                }
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                ) {
                    Text("Abrir en el navegador")
                }
            }
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

/** Script de auto-login para Synology DSM: usa la API SYNO.API.Auth en vez de rellenar el formulario. */
fun buildSynologyLoginJs(account: String, password: String, launchUrl: String): String {
    val usr = account.jsEscape()
    val pwd = password.jsEscape()
    val dst = launchUrl.jsEscape()
    return """(function(){if(!document.querySelector('input[name="username"]')){return;}try{var u=encodeURIComponent('$usr');var p=encodeURIComponent('$pwd');fetch('/webapi/entry.cgi?api=SYNO.API.Auth&version=6&method=login&account='+u+'&passwd='+p+'&session=webui&format=cookie',{credentials:'include'}).then(function(r){return r.json();}).then(function(j){if(j&&j.success){try{if(j.data&&j.data.sid){document.cookie='id='+j.data.sid+'; path=/';}}catch(e){}location.replace('$dst');}}).catch(function(){});}catch(e){}})();"""
}

private fun String.jsEscape(): String =
    replace("\\", "\\\\").replace("'", "\\'")
