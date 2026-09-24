package com.lebeche.monderas.app

import android.app.Application
import com.lebeche.monderas.app.calendario.notif.NotificationPublisher
import com.lebeche.monderas.app.calendario.notif.ReminderScheduler
import com.lebeche.monderas.app.calendario.sync.SyncWorker
import com.lebeche.monderas.app.mail.MailNotifications

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            NotificationPublisher.ensureChannel(this)
            MailNotifications.ensureChannel(this)
            SyncWorker.schedule(this)
            // Reprograma los recordatorios del calendario.
            Thread { ReminderScheduler.rescheduleAll(this) }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
