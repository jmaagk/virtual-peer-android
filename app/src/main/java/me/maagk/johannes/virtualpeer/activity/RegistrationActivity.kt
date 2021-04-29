package me.maagk.johannes.virtualpeer.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.registration.NameInputFragment

class RegistrationActivity : AppCompatActivity(R.layout.activity_registration), NameInputFragment.OnNameEnteredListener {

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        // checking which things still need to be answered by the user
        val nameAvailable = pref.contains(getString(R.string.pref_name)) && !pref.getString(getString(R.string.pref_name), null).isNullOrBlank()

        if(!nameAvailable) {
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

        // the "setup" / "registration" is finished here for now, this will change in the future;
        // a better system for entering multiple values / answering questions will be needed
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

}