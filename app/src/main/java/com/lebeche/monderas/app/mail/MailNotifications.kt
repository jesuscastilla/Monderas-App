package com.lebeche.monderas.app.mail

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lebeche.monderas.app.MainActivity
import com.lebeche.monderas.app.R

object MailNotifications {
    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(MailConfig.CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(MailConfig.CHANNEL_ID, "Correo", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    fun showNewMail(context: Context, from: String, subject: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, MailConfig.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (from.isBlank()) "Nuevo correo" else "Nuevo correo de $from")
            .setContentText(subject)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(MailConfig.NOTIF_ID, n)
    }
}
