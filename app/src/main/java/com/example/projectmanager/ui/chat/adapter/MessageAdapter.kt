package com.example.projectmanager.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.model.MessageStatus
import com.example.projectmanager.data.model.MessageType
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val currentUserId: String,
    private val onAttachmentClick: (Message) -> Unit,
    private val onMessageLongClick: (Message, View) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.type == MessageType.SYSTEM -> VIEW_TYPE_SYSTEM
            message.senderId == currentUserId -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> { // VIEW_TYPE_SYSTEM
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_system, parent, false)
                SystemMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)
        private val messageStatus: ImageView = itemView.findViewById(R.id.image_message_status)
        private val messageImage: ImageView = itemView.findViewById(R.id.image_message_content)
        private val messageContainer: ConstraintLayout = itemView.findViewById(R.id.message_container)
        private val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
        private val fileName: TextView = itemView.findViewById(R.id.file_name)

        fun bind(message: Message) {
            itemView.setOnLongClickListener {
                onMessageLongClick(message, messageContainer)
                true
            }

            when (message.type) {
                MessageType.TEXT -> {
                    messageText.visibility = View.VISIBLE
                    messageImage.visibility = View.GONE
                    fileIcon.visibility = View.GONE
                    fileName.visibility = View.GONE

                    messageText.text = message.content
                }
                MessageType.IMAGE -> {
                    messageText.visibility = View.GONE
                    messageImage.visibility = View.VISIBLE
                    fileIcon.visibility = View.GONE
                    fileName.visibility = View.GONE

                    Glide.with(itemView.context)
                        .load(message.content)
                        .into(messageImage)

                    messageImage.setOnClickListener {
                        onAttachmentClick(message)
                    }
                }
                MessageType.FILE -> {
                    messageText.visibility = View.GONE
                    messageImage.visibility = View.GONE
                    fileIcon.visibility = View.VISIBLE
                    fileName.visibility = View.VISIBLE

                    fileName.text = message.content.substringAfterLast("/")
                    
                    itemView.setOnClickListener {
                        onAttachmentClick(message)
                    }
                }
                else -> {
                    // Should not happen for sent messages
                }
            }

            // Set time
            message.sentAt?.let {
                messageTime.text = formatTime(it)
            }

            // Set status icon
            when (message.status) {
                MessageStatus.SENDING -> messageStatus.setImageResource(R.drawable.ic_sending)
                MessageStatus.SENT -> messageStatus.setImageResource(R.drawable.ic_sent)
                MessageStatus.DELIVERED -> messageStatus.setImageResource(R.drawable.ic_delivered)
                MessageStatus.READ -> messageStatus.setImageResource(R.drawable.ic_read)
                MessageStatus.ERROR -> messageStatus.setImageResource(R.drawable.ic_error)
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)
        private val senderName: TextView = itemView.findViewById(R.id.text_message_sender)
        private val messageImage: ImageView = itemView.findViewById(R.id.image_message_content)
        private val messageContainer: ConstraintLayout = itemView.findViewById(R.id.message_container)
        private val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
        private val fileName: TextView = itemView.findViewById(R.id.file_name)

        fun bind(message: Message) {
            itemView.setOnLongClickListener {
                onMessageLongClick(message, messageContainer)
                true
            }

            // Set sender name
            senderName.text = message.senderName ?: "User"

            when (message.type) {
                MessageType.TEXT -> {
                    messageText.visibility = View.VISIBLE
                    messageImage.visibility = View.GONE
                    fileIcon.visibility = View.GONE
                    fileName.visibility = View.GONE

                    messageText.text = message.content
                }
                MessageType.IMAGE -> {
                    messageText.visibility = View.GONE
                    messageImage.visibility = View.VISIBLE
                    fileIcon.visibility = View.GONE
                    fileName.visibility = View.GONE

                    Glide.with(itemView.context)
                        .load(message.content)
                        .into(messageImage)

                    messageImage.setOnClickListener {
                        onAttachmentClick(message)
                    }
                }
                MessageType.FILE -> {
                    messageText.visibility = View.GONE
                    messageImage.visibility = View.GONE
                    fileIcon.visibility = View.VISIBLE
                    fileName.visibility = View.VISIBLE

                    fileName.text = message.content.substringAfterLast("/")
                    
                    itemView.setOnClickListener {
                        onAttachmentClick(message)
                    }
                }
                else -> {
                    // Should not happen for received messages
                }
            }

            // Set time
            message.sentAt?.let {
                messageTime.text = formatTime(it)
            }
        }
    }

    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_system_message)

        fun bind(message: Message) {
            messageText.text = message.content
        }
    }

    private fun formatTime(date: Date): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}