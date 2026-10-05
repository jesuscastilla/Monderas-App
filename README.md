# Mondera's App

App Android **nativa** (Kotlin + Jetpack Compose) para la **organización de las Monderas**, las
organizadoras del **Colectivo Lebeche**. Con un único usuario y contraseña, permite gestionar desde
pestañas separadas:

- **📅 Calendario** — crear/editar/borrar eventos en Synology Calendar (CalDAV, bidireccional).
- **✉️ Correo** — pantalla nativa con los datos IMAP/SMTP copiables, tutorial paso a paso y botón para abrir la app de correo del móvil (`Intent CATEGORY_APP_EMAIL`).
- **🌐 Web** — acceder al panel STAFF de la web (WebView con auto-login y botón «Guardar»).
- **☁️ Drive** — explorador de archivos nativo del NAS (API Synology File Station: listar, navegar, subir y descargar).
- **🔔 Alertas** — Notificaciones Push en tiempo real gracias a Firebase Cloud Messaging (FCM).

---

## Estado

**🚧 En desarrollo activo (2026-10-01).** La app dispone de **login único** (cuenta compartida
`monderas`), las **cuatro pestañas** (calendario, correo, web y drive), **modo oscuro** con toggle,
**firma de release** y **CI** que compila y publica el APK firmado en cada push.

- **Último release:** `v3.1.0` (versionCode 26) → https://github.com/jesuscastilla/Monderas-App/releases

---

## Identidad

| Campo | Valor |
|---|---|
| **Package ID** | `com.lebeche.calendario` (reutiliza la ficha de Play) |
| **Nombre** | Mondera's App |
| **Plataforma** | Android nativo (Kotlin + Jetpack Compose + Material 3) |
| **minSdk / target / compile** | 28 (Android 9+) / 37 / 37 |
| **Firma** | misma clave del calendario (alias `calendario-lebeche`) |
| **Colores** | Esquema LOGOS (azul Lebeche) |

---

## Tecnología

| Componente | Tecnología |
|---|---|
| UI | Jetpack Compose + Material 3 (modo claro/oscuro) |
| Calendario | OkHttp 5 + biweekly (iCalendar) · CalDAV |
| Correo | Pantalla nativa (credenciales IMAP/SMTP + `Intent CATEGORY_APP_EMAIL`) |
| Web (STAFF) | WebView con auto-login por JS |
| Drive | OkHttp 5 + API Synology File Station (`auth.cgi`/`entry.cgi`) |
| Push | Firebase Cloud Messaging (FCM) para notificaciones |
| Seguridad | Keystore de Android (AES/GCM) + SharedPreferences |
| CI/CD | GitHub Actions (`release.yml`) → APK/AAB firmado a GitHub Releases |

---

## Compilar

```powershell
cd g:\GITHUB\monderas
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\jesus\AppData\Local\Android\Sdk'
.\gradlew.bat :app:assembleDebug      # APK de desarrollo
.\gradlew.bat :app:assembleRelease    # APK firmado
.\gradlew.bat :app:bundleRelease      # AAB (Google Play)
```

Salidas: `app/build/outputs/apk/{debug,release}/`.

---

## Repositorio

- **Local:** `G:\GITHUB\monderas\`
- **GitHub:** https://github.com/jesuscastilla/Monderas-App
- **Contexto global:** `G:\GITHUB\CONTEXT.md`