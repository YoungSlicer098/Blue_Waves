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
import com.dld.bluewaves.SearchUserActivity
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

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
        FirebaseUtils.getOtherUserFromChatroom(model.userIds as List<String>).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val lastMessageSentByMe: Boolean = model.lastMessageSenderId == FirebaseUtils.currentUserId()


                val otherUserModel: UserModel = task.getResult().toObject(UserModel::class.java)!!
                holder.displayNameText.text = otherUserModel.displayName
                if (lastMessageSentByMe && model.lastMessage.length > 20) {
                    holder.lastMessageText.text = "You: " + model.lastMessage.substring(0,20) + "..."
                } else if(lastMessageSentByMe) {
                    holder.lastMessageText.text = "You: " + model.lastMessage
                } else if(model.lastMessage.length > 20){
                    holder.lastMessageText.text = model.lastMessage.substring(0,20) + "..."
                } else{
                    holder.lastMessageText.text = model.lastMessage
                }
                holder.lastMessageTime.text = FirebaseUtils.timestampToString(model.lastMessageTimestamp!!)

                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        AndroidUtils.passUserModelAsIntent(this, otherUserModel)
                        this.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
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