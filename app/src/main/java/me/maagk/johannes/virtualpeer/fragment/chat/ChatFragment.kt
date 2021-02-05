package me.maagk.johannes.virtualpeer.fragment.chat

import androidx.fragment.app.Fragment
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle

class ChatFragment : Fragment(R.layout.fragment_chat), FragmentActionBarTitle {

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_chat)

}