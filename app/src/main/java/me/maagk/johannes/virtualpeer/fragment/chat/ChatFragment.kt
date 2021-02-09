package me.maagk.johannes.virtualpeer.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.fragment.FragmentActionBarTitle

class ChatFragment : Fragment(R.layout.fragment_chat), FragmentActionBarTitle {

    override val actionBarTitle: String
        get() = getString(R.string.nav_drawer_chat)

    class Message(val type: Int, val message: String) {

        companion object {
            const val INCOMING = 0
            const val OUTGOING = 1
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.chatList)

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        val messages = arrayListOf<Message>()
        val adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter

        val inputField: TextInputEditText = view.findViewById(R.id.chatInput)

        val sendButton: FloatingActionButton = view.findViewById(R.id.send)
        sendButton.setOnClickListener {
            messages.add(Message(Message.OUTGOING, inputField.text.toString()))
            messages.add(Message(Message.INCOMING, inputField.text.toString()))
            adapter.notifyDataSetChanged()
        }
    }

    private class ChatAdapter(val messages: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private class IncomingMessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val messageText: TextView = itemView.findViewById(R.id.message)
            private var currentMessage: Message? = null

            fun bind(message: Message) {
                messageText.text = message.message
            }

        }

        private class OutgoingMessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val messageText: TextView = itemView.findViewById(R.id.message)
            private var currentMessage: Message? = null

            fun bind(message: Message) {
                messageText.text = message.message
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType) {
                Message.INCOMING -> {
                    val view = layoutInflater.inflate(R.layout.view_message_incoming, parent, false)
                    IncomingMessageViewHolder(view)
                }

                else -> {
                    val view = layoutInflater.inflate(R.layout.view_message_outgoing, parent, false)
                    OutgoingMessageViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            when(holder.itemViewType) {
                Message.INCOMING -> {
                    (holder as IncomingMessageViewHolder).bind(message)
                }

                Message.OUTGOING -> {
                    (holder as OutgoingMessageViewHolder).bind(message)
                }
            }
        }

        override fun getItemCount(): Int {
            return messages.size
        }

        override fun getItemViewType(position: Int): Int {
            return messages[position].type
        }

    }

}