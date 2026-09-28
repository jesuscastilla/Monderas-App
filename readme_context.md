# readme_context.md — Mondera's App

Contexto técnico del proyecto para un agente de IA (Antigravity / Android Studio).
Si se necesita más infraestructura, credenciales o historia, está en `G:\GITHUB\CONTEXT.md`
(fuera de este repo).

---

## 1. Qué es

App Android **nativa** (Kotlin + Jetpack Compose + Material 3) para la **organización de las
Monderas** (las organizadoras del **Colectivo Lebeche**). Consolida en una sola app: calendario,
correo, acceso a la web y archivos (Drive). Un único usuario y contraseña compartidos.

**Reutiliza la ficha de Google Play** de la antigua app "Calendario Lebeche" (mismo `applicationId`
y misma clave de firma), para actualizar la app existente en vez de crear una ficha nueva.

---

## 2. Identidad y versión

| Campo | Valor |
|---|---|
| `applicationId` | `com.lebeche.calendario` (ficha de Play reutilizada) |
| `namespace` | `com.lebeche.monderas.app` (paquetes fuente) |
| `minSdk` / `targetSdk` / `compileSdk` | 28 / 37 / 37 |
| `versionCode` / `versionName` | 20 / 3.0.0 |
| AGP / Gradle | 9.4.1 / 9.8.0 (Java 17) |
| Plugin Kotlin Compose | 2.2.10 |

> ⚠️ **Gotcha:** `namespace` ≠ `applicationId` (a propósito). Los paquetes fuente son
> `com.lebeche.monderas.app.*`, pero la app se instala como `com.lebeche.calendario`.

---

## 3. Firma (release)

- Keystore propio **del calendario** (alias `calendario-lebeche`), para poder actualizar la ficha de Play.
- `keystore.properties` y `signing.keystore` están en `.gitignore` (NO se versionan).
- CI: `.github/workflows/release.yml` — en cada push a `main` compila `assembleRelease` + `bundleRelease` (APK + AAB) firmados y
  lo publica en GitHub Releases con secretos: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

---

## 4. Funcionalidad

- **Login único** (cuenta compartida `monderas`). La contraseña se guarda cifrada con Android
  Keystore (AES/GCM) y las pestañas autentican solas.
- **📅 Calendario** (pestaña por defecto): CalDAV **bidireccional** (Synology Calendar), vista
  mensual + agenda, eventos recurrentes (RRULE), notificaciones/recordatorios y **exportación al
  calendario del sistema** (`CalendarContract`).
- **✉️ Correo**: WebView a **Roundcube online** (`correo.corrientelebeche.es`) con auto-login.
- **🌐 Web**: WebView al panel STAFF con auto-login por JS.
- **☁️ Drive**: WebView a Synology Drive (DSM) con auto-login (usuario `lebeche`).

---

## 5. Estructura del código

Raíz: `app/src/main/java/com/lebeche/monderas/app/`

```
App.kt                     # Application: crea canales de notificación, programa sync
MainActivity.kt            # gate login/logout + permisos
data/
  Crypto.kt                # cifrado AES/GCM con Android Keystore (alias "monderas_master")
  SessionManager.kt        # login/logout, contraseña cifrada, USUARIO="monderas", PASS_HASH
ui/
  LoginScreen.kt           # pantalla de login
  MonderasApp.kt           # Scaffold + 4 pestañas (enum MonderasTab)
  WebUrls.kt               # URLs: panel STAFF + Roundcube + Synology Drive
  components/WebViewScreen.kt
  screens/CalendarioScreen.kt, CorreoScreen.kt, WebScreen.kt, DriveScreen.kt
  theme/Color.kt           # colores LOGOS (AzulTinte, AzulMadre, ...)
  theme/Theme.kt           # MonderasTheme (light/dark) + tipografía
  theme/Type.kt           # MonderasTypography (Courgette/Garet/Open Sans)
calendario/                # paquete AUTÓNOMO portado de la antigua "Calendario Lebeche"
  Repository.kt            # punto de acceso a datos + lógica de sync (push/pull)
  cal/SystemCalendarSync.kt
  caldav/CalDavClient.kt, DavXmlParser.kt, ICalHelper.kt
  data/Db.kt, Models.kt, Crypto.kt, Prefs.kt
  notif/BootReceiver.kt, NotificationPublisher.kt, ReminderScheduler.kt
  sync/SyncWorker.kt
  ui/CalendarApp.kt, MainScreen.kt, MainScreenUI.kt, EventDetailScreen.kt,
     EventEditScreen.kt, ReminderUi.kt, SettingsScreen.kt, WelcomeScreen.kt
```

**Manifest** (`app/src/main/AndroidManifest.xml`): `MainActivity` (launcher), receivers
`calendario.notif.NotificationPublisher` y `calendario.notif.BootReceiver`. Permisos: INTERNET,
ACCESS_NETWORK_STATE, READ/WRITE_CALENDAR, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED,
SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM.

---

## 6. Servicios externos (endpoints)

| Servicio | Valor |
|---|---|
| **CalDAV** (Synology Calendar) | `https://pelotxo.synology.me:5001/caldav/` · usuario `lebeche` |
| **Webmail** (Roundcube) | `https://correo.corrientelebeche.es/` · usuario `monderas` |
| **Synology Drive** (DSM) | `https://pelotxo.synology.me:5001/` · usuario `lebeche` |
| **Web STAFF** | `https://www.corrientelebeche.es/lebeche/admin/index.php` |

Definidos en: `Repository.DEFAULT_CALDAV_URL`, `CalendarApp.CALDAV_USER`, `WebUrls`.

---

## 7. Credenciales / secretos (dónde están)

- **Usuario compartido:** `monderas` (`SessionManager.USUARIO`).
- **Contraseña compartida:** NO está en claro en este repo. Hay un hash SHA-256 en
  `SessionManager.PASS_HASH`. El valor en claro está en `G:\GITHUB\CONTEXT.md` (es la misma de
  SLiMS y del buzón de correo).
- **Firma:** `keystore.properties` + `signing.keystore` (gitignored) y secretos de GitHub.

---

## 8. Marca / colores (LOGOS)

Esquema azul Lebeche, ya aplicado en `ui/theme/Color.kt` y `ui/theme/Theme.kt`:

| Token | Hex |
|---|---|
| AzulTinte | `#A9D9ED` |
| AzulMadre | `#8DCDE7` |
| AzulSombra | `#3B758B` |
| AzulSombraOscura | `#395661` |
| AzulMasOscuro | `#26373E` |
| Ámbar (acento) | `#E8A33D` |
| Papel / Tinta (neutros) | `#F5F5F0` / `#141414` |

Tipografías de marca aplicadas en `ui/theme/Type.kt` (`MonderasTypography`): Courgette (display/logo), Garet (titulares), Open Sans (cuerpo/etiquetas). Fuentes en `res/font/`.

> No usar la paleta antigua marrón de Barrioteca (`PwaCream`, `PwaPrimary #8A5A00`, etc.).

---

## 9. Dependencias clave

- Compose BOM `2026.09.00` + Material 3 + `material-icons-extended`.
- Calendario: `okhttp:5.5.0`, `biweekly:0.6.8` (iCalendar), `work-runtime-ktx:2.12.0`,
  `kotlinx-coroutines-android:1.11.0`.

---

## 10. Compilar

```powershell
cd g:\GITHUB\monderas
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\jesus\AppData\Local\Android\Sdk'
.\gradlew.bat :app:assembleDebug      # APK de desarrollo
.\gradlew.bat :app:assembleRelease    # APK firmado
.\gradlew.bat :app:bundleRelease      # AAB (Google Play)
```

Salidas: `app/build/outputs/apk/{debug,release}/` y `app/build/outputs/bundle/release/`.

---

## 11. Convenciones y gotchas para el agente

- **No cambiar `applicationId`** (rompería la ficha de Play reutilizada). El `namespace` sí es
  `com.lebeche.monderas.app` y los paquetes fuente también.
- **Nombre mostrado** "Mondera's App" viene de `res/values/strings.xml` (`app_name`), con el
  apóstrofo escapado como `\'` (obligatorio en XML).
- **Release usa R8/minify**: las reglas necesarias ya están en `app/proguard-rules.pro` (biweekly,
  OkHttp). Si añades librerías con reflexión, añade sus reglas.
- **Calendario**: es un paquete portado autónomo con su propia SQLite (`calendario/data/Db.kt`).
  No confundir `calendario/data/Crypto.kt` con `data/Crypto.kt` (son distintos).
- El calendario es una pestaña (no una app aparte); el tema activo es `MonderasTheme` (LOGOS),
  no el antiguo `CalendarioLebecheTheme` (ya eliminado).
- **Entorno**: compileSdk/targetSdk 37 + Gradle 9.8.0 → requiere **JDK 17** (`sourceCompatibility`/`targetCompatibility` en `app/build.gradle`).
- **Panel STAFF (pestaña Web)**: el botón "Guardar" de la app inyecta JS que pulsa `button.btn--guardar` del panel activo (`.admin__panel.activo .btn--guardar`). El código del panel está en `G:\GITHUB\LEBECHE\admin\` (`index.php`, `admin.js`, `guardar.php`). Los `alert()` JS se muestran como Toast vía `onJsAlert`.
- Contexto global (infraestructura, credenciales, historia, otras apps): `G:\GITHUB\CONTEXT.md`.