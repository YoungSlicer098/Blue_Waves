package com.dld.bluewaves.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.R
import com.dld.bluewaves.model.ChatMessageModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.dld.bluewaves.utils.getOtherProfilePicStorageRef
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatMessageModel>,
    private val context: Context,
    private val profilePic: String = ""
) : FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options) {

    class ChatModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_text_view)
        val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_text_view)
        val profilePicImageView: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
    }

    override fun onBindViewHolder(
        holder: ChatModelViewHolder,
        position: Int,
        model: ChatMessageModel
    ) {
        if(profilePic != ""){
            holder.profilePicImageView.setImageResource(AndroidUtils.selectPicture(profilePic))
        }else {
            getOtherProfilePicStorageRef(model.senderId).downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uri: Uri = task.result
                    AndroidUtils.setProfilePic(context, uri, holder.profilePicImageView)
                } else {
                    // Optionally, set a placeholder or fallback image for failed loads
                    holder.profilePicImageView.setImageResource(R.drawable.profile_white)
                }
            }
        }

        if (model.senderId.equals(FirebaseUtils.currentUserId())) {
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.leftChatLayout.visibility = View.GONE
            holder.profilePicImageView.visibility = View.GONE
            holder.rightChatTextView.text = model.message
        } else {
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.rightChatLayout.visibility = View.GONE
            holder.leftChatTextView.text = model.message
            holder.profilePicImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_recycler_row, parent, false)
        return ChatModelViewHolder(view)
    }
}