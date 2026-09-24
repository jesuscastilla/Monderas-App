package com.lebeche.monderas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestiona la sesión de la app (única cuenta compartida de las Monderas).
 * Guarda la contraseña cifrada (Keystore) para que las pestañas CalDAV/IMAP/web
 * puedan autenticarse solas tras el login.
 */
object SessionManager {
    private const val PREFS = "monderas_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER = "user"
    private const val KEY_PASS_ENC = "password_enc"

    /** Usuario único y hash SHA-256 de la contraseña compartida (no se guarda en claro). */
    const val USUARIO = "monderas"
    private const val PASS_HASH = "8bf948d59abd7cf4271031aac2026d196fea3f0d2d2f31a3d205cab98110066f"

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun init(context: Context) {
        _isLoggedIn.value = prefs(context).getBoolean(KEY_LOGGED_IN, false)
    }

    fun login(context: Context, usuario: String, contrasena: String): Boolean {
        if (usuario != USUARIO || !verificaContrasena(contrasena)) return false
        prefs(context).edit {
            putBoolean(KEY_LOGGED_IN, true)
            putString(KEY_USER, usuario)
            putString(KEY_PASS_ENC, Crypto.encrypt(contrasena))
        }
        _isLoggedIn.value = true
        return true
    }

    fun logout(context: Context) {
        prefs(context).edit { clear() }
        _isLoggedIn.value = false
    }

    /** Contraseña compartida en claro (para autenticar CalDAV / IMAP / web). */
    fun contrasena(context: Context): String {
        val enc = prefs(context).getString(KEY_PASS_ENC, "").orEmpty()
        return if (enc.isEmpty()) "" else Crypto.decrypt(enc)
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun verificaContrasena(contrasena: String): Boolean =
        sha256(contrasena) == PASS_HASH

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
