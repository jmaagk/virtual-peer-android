package me.maagk.johannes.virtualpeer.fragment.registration

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R

class NameInputFragment : Fragment(R.layout.fragment_name_input) {

    interface OnNameEnteredListener {
        fun onNameEntered(name: String)
    }

    private lateinit var nameInput: TextInputEditText

    lateinit var onNameEnteredListener: OnNameEnteredListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.nameInput)
        nameInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                if(nameInput.text.isNullOrBlank()) {
                    Toast.makeText(requireContext(), R.string.registration_enter_name_error_empty_name, Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                if(::onNameEnteredListener.isInitialized)
                    onNameEnteredListener.onNameEntered(nameInput.text.toString())
                return@setOnEditorActionListener true
            }

            false
        }
    }

}