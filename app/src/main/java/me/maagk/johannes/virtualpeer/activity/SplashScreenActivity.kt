package me.maagk.johannes.virtualpeer.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import me.maagk.johannes.virtualpeer.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making sure the splash screen follows the system's theme (light / dark)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        PreferenceManager.setDefaultValues(this, R.xml.preferences_root, false)

        // checking if this is the first time the app is launched
        // if this is the case, the current time will be saved for later use
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var firstLaunchTime = sharedPreferences.getLong(getString(R.string.pref_first_launch_time), -1)

        val firstLaunch = if(firstLaunchTime == -1L) {
            firstLaunchTime = System.currentTimeMillis()
            sharedPreferences.edit(commit = true) {
                putLong(getString(R.string.pref_first_launch_time), firstLaunchTime)
            }
            true
        } else {
            false
        }

        val nextActivityIntent = Intent()
        if(firstLaunch) {
            TODO("implement first launch behavior")
        } else {
            nextActivityIntent.setClass(this, MainActivity::class.java)
        }

        startActivity(nextActivityIntent)
        finish()
    }

}