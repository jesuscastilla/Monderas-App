# Mondera's App

App Android **nativa** (Kotlin + Jetpack Compose) para la **organización de las Monderas**, las
organizadoras del **Colectivo Lebeche**. Con un único usuario y contraseña, permite gestionar desde
pestañas separadas:

- **📅 Calendario** — crear/editar/borrar eventos en Synology Calendar (CalDAV, bidireccional).
- **✉️ Correo** — leer y responder el buzón `monderas@corrientelebeche.es` (IMAP IDLE push; solo en la app Android).
- **🌐 Web** — acceder al panel STAFF de la web (WebView con auto-login).

---

## Estado

**🚧 En desarrollo activo (2026-09-27).** La app **ya se está programando**: dispone de
**login único** (cuenta compartida `monderas`), las **tres pestañas** (calendario, correo y web)
funcionando, **firma de release** y **CI** que compila y publica el APK firmado en cada push.

- **Último release:** `v3.0.0` (versionCode 16) → https://github.com/jesuscastilla/Monderas-App/releases

---

## Identidad

| Campo | Valor |
|---|---|
| **Package ID** | `com.lebeche.calendario` (reutiliza la ficha de Play) |
| **Nombre** | Mondera's App |
| **Plataforma** | Android nativo (Kotlin + Jetpack Compose + Material 3) |
| **minSdk / target / compile** | 28 (Android 9+) / 36 / 36 |
| **Firma** | misma clave del calendario (alias `calendario-lebeche`) |
| **Colores** | Esquema LOGOS (azul Lebeche) |

---

## Tecnología

| Componente | Tecnología |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Calendario | OkHttp 4 + biweekly (iCalendar) · CalDAV |
| Correo | JavaMail (`com.sun.mail`, IMAP IDLE) |
| Seguridad | Keystore de Android (AES/GCM) + SQLite propia |
| CI/CD | GitHub Actions (`release.yml`) → APK firmado a GitHub Releases |

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
