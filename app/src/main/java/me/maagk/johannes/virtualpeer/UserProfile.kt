package me.maagk.johannes.virtualpeer

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import me.maagk.johannes.virtualpeer.Utils.Companion.containsNonNullAndNonBlankValue

class UserProfile @JvmOverloads constructor(
        private val context: Context,
        private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)) {

    private val nameKey = context.getString(R.string.pref_name)
    private val dateOfBirthKey = context.getString(R.string.pref_date_of_birth)
    private val placeOfBirthKey = context.getString(R.string.pref_place_of_birth)
    private val emailKey = context.getString(R.string.pref_email)
    private val identifierKey = context.getString(R.string.pref_identifier)

    var name: String?
        get() = pref.getString(nameKey, null)
        set(value) = pref.edit().putString(nameKey, value).apply()

    var dateOfBirth: String?
        get() = pref.getString(dateOfBirthKey, null)
        set(value) = pref.edit().putString(dateOfBirthKey, value).apply()

    var placeOfBirth: String?
        get() = pref.getString(placeOfBirthKey, null)
        set(value) = pref.edit().putString(placeOfBirthKey, value).apply()

    var email: String?
        get() = pref.getString(emailKey, null)
        set(value) = pref.edit().putString(emailKey, value).apply()

    var identifier: String?
        get() = pref.getString(identifierKey, null)
        set(value) = pref.edit().putString(identifierKey, value).apply()

    fun isNameAvailable(): Boolean = pref.containsNonNullAndNonBlankValue(context.getString(R.string.pref_name))
    fun isEmailAvailable(): Boolean = pref.containsNonNullAndNonBlankValue(context.getString(R.string.pref_email))
    fun isIdentifierAvailable(): Boolean = pref.containsNonNullAndNonBlankValue(context.getString(R.string.pref_identifier))

}