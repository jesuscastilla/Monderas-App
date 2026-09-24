package com.lebeche.monderas.app.mail

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lebeche.monderas.app.MainActivity
import com.lebeche.monderas.app.R
import com.lebeche.monderas.app.data.SessionManager
import com.sun.mail.imap.IMAPFolder
import javax.mail.Folder
import javax.mail.Store
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent
import javax.mail.internet.InternetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Servicio en segundo plano que mantiene IMAP IDLE y avisa de correo nuevo. */
class ImapIdleService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var store: Store? = null
    @Volatile private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildForegroundNotification())
        if (!running) {
            running = true
            scope.launch { idleLoop() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        runCatching { store?.close() }
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun idleLoop() {
        val password = SessionManager.contrasena(this)
        if (password.isEmpty()) {
            stopSelf()
            return
        }
        while (running) {
            try {
                val s = MailRepository.connectStore(MailConfig.EMAIL, password)
                store = s
                try {
                    val inbox = s.getFolder("INBOX") as IMAPFolder
                    inbox.open(Folder.READ_ONLY)
                    val listener = object : MessageCountAdapter() {
                        override fun messagesAdded(e: MessageCountEvent) {
                            for (m in e.messages) {
                                runCatching {
                                    val from = (m.from?.firstOrNull() as? InternetAddress)?.address.orEmpty()
                                    MailNotifications.showNewMail(this@ImapIdleService, from, m.subject.orEmpty())
                                }
                            }
                        }
                    }
                    inbox.addMessageCountListener(listener)
                    while (running) {
                        inbox.idle()
                    }
                    inbox.removeMessageCountListener(listener)
                } finally {
                    runCatching { s.close() }
                }
            } catch (_: Exception) {
                // conexión caída: se reintenta
            } finally {
                store = null
            }
            delay(RETRY_MS)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(MailConfig.CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(MailConfig.CHANNEL_ID, "Correo", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, MailConfig.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Correo conectado")
            .setContentText("Recibiendo correo en tiempo real")
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1000
        private const val ACTION_STOP = "stop"
        private const val RETRY_MS = 15000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ImapIdleService::class.java))
        }

        fun stop(context: Context) {
            context.startService(Intent(context, ImapIdleService::class.java).setAction(ACTION_STOP))
        }
    }
}
