package me.maagk.johannes.virtualpeer.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils.Companion.containsNonNullAndNonBlankValue
import me.maagk.johannes.virtualpeer.fragment.registration.NameInputFragment
import me.maagk.johannes.virtualpeer.fragment.settings.ProfileFragment

class RegistrationActivity : AppCompatActivity(R.layout.activity_registration), NameInputFragment.OnNameEnteredListener, ProfileFragment.OnLastEntryEnteredListener {

    private lateinit var pref: SharedPreferences

    private val emailAvailable: Boolean
        get() = pref.containsNonNullAndNonBlankValue(getString(R.string.pref_email))

    private val identifierAvailable: Boolean
        get() = pref.containsNonNullAndNonBlankValue(getString(R.string.pref_identifier))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        // checking which things still need to be answered by the user
        val nameAvailable = pref.containsNonNullAndNonBlankValue(getString(R.string.pref_name))

        if(nameAvailable && emailAvailable && identifierAvailable) {
            startMainActivity()
        } else if(nameAvailable) {
            showProfileFragment()
        } else {
            val nameInputFragment = NameInputFragment()
            nameInputFragment.onNameEnteredListener = this

            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, nameInputFragment).commit()
        }
    }

    override fun onNameEntered(name: String) {
        // saving the name entered by the user to the app's preferences
        pref.edit(commit = true) {
            putString(getString(R.string.pref_name), name)
        }

        // depending on how much info the user already entered, either the MainActivity or
        // the profile fragment to enter more info will be shown
        if(emailAvailable && identifierAvailable)
            startMainActivity()
        else
            showProfileFragment()
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun showProfileFragment() {
        val profileFragment = ProfileFragment()
        profileFragment.onLastEntryEnteredListener = this

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, profileFragment).commit()
    }

    override fun onLastEntryEntered() {
        if(!emailAvailable) {
            Toast.makeText(this, R.string.profile_input_email_error_empty, Toast.LENGTH_LONG).show()
            return
        }

        if(!identifierAvailable) {
            Toast.makeText(this, R.string.profile_input_identifier_error_empty, Toast.LENGTH_LONG).show()
            return
        }

        startMainActivity()
    }

}