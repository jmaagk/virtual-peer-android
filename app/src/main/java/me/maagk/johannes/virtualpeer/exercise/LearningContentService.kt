package me.maagk.johannes.virtualpeer.exercise

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import me.maagk.johannes.virtualpeer.VirtualPeerApp
import java.util.*
import java.util.concurrent.TimeUnit

class LearningContentService : Service() {

    private lateinit var learningContent: LearningContent

    private lateinit var notificationManager: NotificationManager
    private var notificationId = -1

    private var startTime = 0L

    private val timer = Timer()

    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val learningContentExtra = intent?.extras?.getParcelable<LearningContent>("learningContent")
        if(learningContentExtra != null)
            learningContent = learningContentExtra

        val notification = buildNotification(TimeUnit.MINUTES.toMillis(learningContent.durationMinutes.toLong()))
        notificationId = if(learningContent is LearningContent.Break)
            VirtualPeerApp.NOTIFICATION_ID_POMODORO_BREAK
        else
            VirtualPeerApp.NOTIFICATION_ID_POMODORO_CONTENT

        startForeground(notificationId, notification)
        intent?.let {
            it.extras?.let { bundle ->
                startTime = bundle.getLong("startTime", -1)
            }
        }

        timer.scheduleAtFixedRate(NotificationTask(), 0, 250)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildNotification(timeLeft: Long): Notification {
        return PomodoroChatExercise.Notification(this, learningContent, timeLeft).build()
    }

    private fun sendNotification(timeLeft: Long) {
        val notification = buildNotification(timeLeft)
        notificationManager.notify(notificationId, notification)
    }

    private inner class NotificationTask : TimerTask() {

        override fun run() {
            val endTime = TimeUnit.MINUTES.toMillis(learningContent.durationMinutes.toLong()) + startTime
            val timeLeft = endTime - System.currentTimeMillis()

            if(timeLeft < 0) {
                cancel()
                stopSelf()
                return
            }

            sendNotification(timeLeft)
        }

    }

}