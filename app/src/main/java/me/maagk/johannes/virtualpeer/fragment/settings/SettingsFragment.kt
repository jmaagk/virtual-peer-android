package me.maagk.johannes.virtualpeer.fragment.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle

class SettingsFragment : PreferenceFragmentCompat(), FragmentActionBarTitle {

    companion object {
        const val TAG = "settings"
    }

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_settings)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_root)
    }



}