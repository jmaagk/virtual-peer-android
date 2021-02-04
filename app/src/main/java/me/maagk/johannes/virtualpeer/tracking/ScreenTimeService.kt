package me.maagk.johannes.virtualpeer.tracking

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ScreenTimeService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}