package com.lebeche.monderas.app.ui

object WebUrls {
    /** Pestaña Web: panel de administración (STAFF). */
    const val WEB = "https://www.corrientelebeche.es/lebeche/admin/index.php"

    /** Pestaña Correo: webmail Roundcube online. */
    const val ROUNDCUBE = "https://correo.corrientelebeche.es/"

    /** Usuario del login de Roundcube (Hostalia pide el email completo). */
    const val ROUNDCUBE_USER = "monderas@corrientelebeche.es"

    /** Pestaña Drive: abre directamente la app Synology Drive del DSM (cuenta lebeche). */
    const val DRIVE = "https://pelotxo.synology.me:5001/?launchApp=SYNO.SDS.Drive.Application"

    /**
     * UA de ESCRITORIO para Drive. DSM detecta cualquier UA con "Android"/"Mobile" y sirve la
     * UI móvil legacy ("DSM mobile", Sencha Touch), que arranca con una máscara de "cargando"
     * y luego se queda en blanco. Con un UA de escritorio DSM sirve el escritorio DSM 7 moderno.
     */
    const val DRIVE_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /** Usuario de Synology para Drive. */
    const val DRIVE_USER = "lebeche"
}
