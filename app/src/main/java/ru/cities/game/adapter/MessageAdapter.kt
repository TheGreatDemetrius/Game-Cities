package ru.cities.game.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.cities.game.databinding.ItemBinding

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageHolder>() {
    private val messageList = mutableListOf<MessageModel>()

    inner class MessageHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageHolder(binding)
    }

    /** The view holder selects the message type */
    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val item = messageList[position]
        with(holder.binding) {
            val incoming = tvIncoming
            val outgoing = tvOutgoing
            if (item.id) {
                incoming.visibility = View.GONE
                setFlag(outgoing, item)
            } else {
                outgoing.visibility = View.GONE
                setFlag(incoming, item)
            }
        }
    }

    /** Setting the flag and displaying the desired message type */
    private fun setFlag(coming: TextView, item: MessageModel) {
        coming.apply {
            text = item.name
            try {
                val drawableId: Int = context.resources.getIdentifier(
                    "ic_${item.flag}",
                    "drawable",
                    context.packageName
                )
                setCompoundDrawablesRelativeWithIntrinsicBounds(drawableId, 0, 0, 0)
            } catch (e: Exception) {
                System.err.println("The flag couldn't be found.")
            }
            visibility = View.VISIBLE
        }
    }

    /** Updating the number of elements */
    override fun getItemCount(): Int = messageList.size

    /** Inserting a message into the RV */
    fun insertMessage(message: MessageModel) {
        this.messageList.add(message)
        notifyItemInserted(messageList.size)
    }
}
