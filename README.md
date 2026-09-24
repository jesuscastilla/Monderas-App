# Mondera's App

Aplicación para las **Monderas** (las organizadoras de la asociación Lebeche). Permitirá, con un único usuario y contraseña, gestionar desde pestañas separadas:

- **📅 Calendario** — crear/editar/borrar eventos en Synology Calendar (CalDAV).
- **🌐 Web** — editar los contenidos de la web de Lebeche (`data/*.json`).
- **✉️ Correo** — leer y responder el buzón `monderas@corrientelebeche.es` (solo en la app Android, no en la web).

Todo con **sincronización bidireccional** y cada proyecto en su propia pestaña.

---

## Estado

**⏸️ Pausado (2026-09-23).** Todavía no hay código: este repositorio es el punto de partida donde se construirá el proyecto. Las apps actuales (Barrioteca y Calendario Lebeche) ya funcionan y se sincronizan, así que se decidió no hacer un cambio grande por ahora.

---

## Qué será (resumen técnico)

| Pieza | Detalle |
|---|---|
| **Backend** | `api/` en PHP, desplegado en `/volume1/web/monderas/api/` (el NAS solo usa PHP) |
| **Web** | `web/` React + Vite + TS + Tailwind → `/volume1/web/monderas/` |
| **Android** | Se amplía `calendario-lebeche` (rebautizada **Mondera's App**), manteniendo `applicationId com.lebeche.calendario` |
| **Login** | usuario `monderas` (hash PBKDF2 en `api/config.php`, no versionado) |

---

## Decisiones tomadas

- **Calendario manda**: la web se genera como espejo desde CalDAV (`programacion-sync.php`).
- **Online-only** (sin cola offline).
- **Correo solo en la app Android** (Jakarta Mail: IMAP 993 + SMTP 587), no en la web.
- **`applicationId` de Android mantenido** (misma ficha de Play y misma firma).
- **SLiMS**: solo se toca README/docs, nunca código.

---

## Próximo paso

Cuando se retome, el primer paso es **construir el backend PHP** (`api/`):

- `auth.php` — login PBKDF2 + CSRF + token para Android.
- `web.php` — lectura/escritura de `data/*.json` (con escritura atómica).
- `calendario.php` — proxy CalDAV (PROPFIND / REPORT / PUT / DELETE).
- `verificar.php` — diagnóstico (como `PWA/diagnostico.php`).

Es la pieza común que necesitan tanto la web como el móvil.

> Antes de empezar, conviene revisar las "Decisiones tomadas" por si algo cambió desde el pause. Todo el contexto e infraestructura está en `G:\GITHUB\CONTEXT.md`.
