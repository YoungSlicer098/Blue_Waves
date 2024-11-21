package com.dld.bluewaves.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.ChatActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RecentChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatRoomModel>,
                                private val context: Context
) : FirestoreRecyclerAdapter<ChatRoomModel, RecentChatRecyclerAdapter.ChatRoomModelViewHolder>(options) {

    init {
        setHasStableIds(true)
    }

    class ChatRoomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        val displayNameText: TextView = itemView.findViewById(R.id.display_name_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
    }

    override fun onBindViewHolder(holder: ChatRoomModelViewHolder, position: Int, model: ChatRoomModel) {
        // Fetch the other user's details based on chat room's userIds
        FirebaseUtils.getOtherUserFromChatroom(model.userIds).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val otherUserModel = task.result.toObject(UserModel::class.java)

                    if (otherUserModel != null) {
                        // Display Name
                        holder.displayNameText.text = if (otherUserModel.userId == FirebaseUtils.currentUserId()) {
                            "${otherUserModel.displayName} (Me)"
                        } else {
                            otherUserModel.displayName
                        }

                        // Last Chat Log
                        val lastMessageSentByMe = model.lastMessageSenderId == FirebaseUtils.currentUserId()
                        holder.lastMessageText.text = when {
                            lastMessageSentByMe && model.lastMessage.length > 20 -> "You: ${model.lastMessage.substring(0, 20)}..."
                            lastMessageSentByMe -> "You: ${model.lastMessage}"
                            model.lastMessage.length > 20 -> "${model.lastMessage.substring(0, 20)}..."
                            else -> model.lastMessage
                        }

                        // Last Interaction or Time Message
                        holder.lastMessageTime.text = FirebaseUtils.timestampToString(model.lastMessageTimestamp!!)

                        // Set OnClickListener to navigate to ChatActivity
                        holder.itemView.setOnClickListener {
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                AndroidUtils.passUserModelAsIntent(this, otherUserModel)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    } else {
                        // Handle case where user data is missing or null
                        bindEmptyState(holder)
                    }
                } else {
                    // Handle Firestore errors or empty data
                    bindEmptyState(holder)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch user data
                exception.printStackTrace()
                bindEmptyState(holder)
            }
    }

    // Helper function to handle empty or invalid user data
    private fun bindEmptyState(holder: ChatRoomModelViewHolder) {
        holder.displayNameText.text = "Unknown User"
        holder.lastMessageText.text = "No recent messages"
        holder.lastMessageTime.text = "--"
        holder.itemView.setOnClickListener(null) // Disable click
    }


    override fun getItemId(position: Int): Long {
        return snapshots.getSnapshot(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatRoomModelViewHolder(view)
    }
}