package me.maagk.johannes.virtualpeer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class VirtualPeerApp : Application() {

    companion object {
        const val CHANNEL_POMODORO = "pomodoro_channel"

        const val NOTIFICATION_ID_POMODORO_CONTENT = 1
        const val NOTIFICATION_ID_POMODORO_BREAK = 2
        const val NOTIFICATION_ID_POMODORO_FINISH = 3
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // creating notification channels for API >= 26
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // creating the pomodoro exercise's notification channel
            val pomodoroChannel = NotificationChannel(CHANNEL_POMODORO, getString(R.string.pomodoro_notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
            pomodoroChannel.description = getString(R.string.pomodoro_notification_channel_description)
            //pomodoroChannel.sound // TODO: add sound

            notificationManager.createNotificationChannel(pomodoroChannel)
        }
    }

}