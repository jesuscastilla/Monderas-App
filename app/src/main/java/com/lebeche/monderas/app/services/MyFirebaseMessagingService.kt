package com.lebeche.monderas.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lebeche.monderas.app.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Mondera's App"
        val mensaje = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""

        mostrarNotificacion(titulo, mensaje)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí podrías enviar el token al NAS si quisieras notificar 1 a 1, 
        // pero vamos a usar subscripción a temas por simplicidad.
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val channelId = "monderas_push_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos y Novedades",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            // Este icono necesita existir en tu proyecto (usaremos un fallback de momento)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
