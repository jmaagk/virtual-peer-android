package me.maagk.johannes.virtualpeer.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.view.ProfileIconView

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    companion object {
        const val TAG = "profile"
    }

    interface OnLastEntryEnteredListener {
        fun onLastEntryEntered()
    }

    class InputPart(rootLayout: ViewGroup, title: String, inputType: Int) {

        val titleText: TextView = rootLayout.findViewById(R.id.inputTitle)
        val inputField: TextInputEditText = rootLayout.findViewById(R.id.input)

        init {
            titleText.text = title
            inputField.inputType = inputType
        }

    }

    private lateinit var profileIcon: ProfileIconView
    private lateinit var usernameText: TextView

    private lateinit var nameInputPart: InputPart
    private lateinit var dateOfBirthInputPart: InputPart
    private lateinit var placeOfBirthInputPart: InputPart
    private lateinit var emailInputPart: InputPart
    private lateinit var identifierInputPart: InputPart

    private lateinit var pref: SharedPreferences

    lateinit var onLastEntryEnteredListener: OnLastEntryEnteredListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        profileIcon = view.findViewById(R.id.profileIcon)
        usernameText = view.findViewById(R.id.username)

        val username = pref.getString(getString(R.string.pref_name), null)
        if(!username.isNullOrBlank()) {
            usernameText.text = username
            profileIcon.usernameChar = username[0]
        }

        nameInputPart = InputPart(view.findViewById(R.id.nameInputLayout), getString(R.string.profile_input_name_title),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME)

        nameInputPart.inputField.addTextChangedListener { _ ->
            if(!nameInputPart.inputField.text.isNullOrBlank()) {
                profileIcon.usernameChar = nameInputPart.inputField.text.toString()[0]

                usernameText.text = nameInputPart.inputField.text.toString()
            }
        }

        dateOfBirthInputPart = InputPart(view.findViewById(R.id.dateOfBirthInputLayout), getString(R.string.profile_input_date_of_birth_title),
            InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE)

        placeOfBirthInputPart = InputPart(view.findViewById(R.id.placeOfBirthInputLayout), getString(R.string.profile_input_place_of_birth_title), InputType.TYPE_CLASS_TEXT)

        emailInputPart = InputPart(view.findViewById(R.id.emailInputLayout), getString(R.string.profile_input_email_title),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        identifierInputPart = InputPart(view.findViewById(R.id.identifierInputLayout), getString(R.string.profile_input_identifier_title), InputType.TYPE_CLASS_TEXT)
        
        identifierInputPart.inputField.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                saveValues()
                if(::onLastEntryEnteredListener.isInitialized)
                    onLastEntryEnteredListener.onLastEntryEntered()
            }
            false
        }
    }
    
    private fun saveValues() {
        pref.edit(commit = true) {
            // TODO: validate the correctness of all inputs

            val nameInput = nameInputPart.inputField.text
            if(!nameInput.isNullOrBlank())
                putString(getString(R.string.pref_name), nameInput.toString())

            val dateOfBirthInput = dateOfBirthInputPart.inputField.text
            if(!dateOfBirthInput.isNullOrBlank())
                putString(getString(R.string.pref_date_of_birth), dateOfBirthInput.toString())

            val placeOfBirthInput = placeOfBirthInputPart.inputField.text
            if(!placeOfBirthInput.isNullOrBlank())
                putString(getString(R.string.pref_place_of_birth), placeOfBirthInput.toString())

            val emailInput = emailInputPart.inputField.text
            if(!emailInput.isNullOrBlank())
                putString(getString(R.string.pref_email), emailInput.toString())

            val identifierInput = identifierInputPart.inputField.text
            if(!identifierInput.isNullOrBlank())
                putString(getString(R.string.pref_identifier), identifierInput.toString())
        }
    }
    
    override fun onPause() {
        super.onPause()

        saveValues()
    }

    override fun onResume() {
        super.onResume()

        nameInputPart.inputField.setText(pref.getString(getString(R.string.pref_name), ""))
        dateOfBirthInputPart.inputField.setText(pref.getString(getString(R.string.pref_date_of_birth), ""))
        placeOfBirthInputPart.inputField.setText(pref.getString(getString(R.string.pref_place_of_birth), ""))
        emailInputPart.inputField.setText(pref.getString(getString(R.string.pref_email), ""))
        identifierInputPart.inputField.setText(pref.getString(getString(R.string.pref_identifier), ""))
    }
}