package com.dld.bluewaves.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.ChatActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.model.AnnouncementModel
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AnnouncementRecyclerAdapter(
    options: FirestoreRecyclerOptions<AnnouncementModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<AnnouncementModel, AnnouncementRecyclerAdapter.AnnouncementModelViewHolder>(
    options
) {

    init {
        setHasStableIds(true)
    }

    class AnnouncementModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val role: TextView = itemView.findViewById(R.id.role_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        val displayNameText: TextView = itemView.findViewById(R.id.display_name_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
        val message: TextView = itemView.findViewById(R.id.message_text)
    }

    override fun onBindViewHolder(
        holder: AnnouncementModelViewHolder,
        position: Int,
        model: AnnouncementModel
    ) {
        FirebaseUtils.getUserAnnouncementReference(model.userId).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val userModel = it.result.toObject(UserModel::class.java)
                holder.role.text = userModel?.role?.uppercase()
                holder.displayNameText.text = userModel?.displayName
                holder.message.text = model.message

                if(userModel?.profilePic != ""){
                    holder.profilePic.setImageResource(AndroidUtils.selectPicture(userModel?.profilePic!!))
                }else {
                    FirebaseUtils.getOtherProfilePicStorageRef(model.userId).downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uri: Uri = task.result
                            AndroidUtils.setProfilePic(context, uri, holder.profilePic)
                        } else {
                            // Optionally, set a placeholder or fallback image for failed loads
                            holder.profilePic.setImageResource(R.drawable.profile_white)
                        }
                    }
                }



                // Convert Firebase.Timestamp to milliseconds
                val timestamp = model.timestamp?.toDate()?.time ?: System.currentTimeMillis()

                // Format the timestamp to relative time
                val relativeTime = TimeUtils.getRelativeTime(context, timestamp)
                holder.lastMessageTime.text = relativeTime

                holder.itemView.setOnLongClickListener {
                    // Show an AlertDialog to confirm deletion
                    AlertDialog.Builder(context)
                        .setTitle("Delete Announcement")
                        .setMessage("Are you sure you want to delete this announcement?")
                        .setPositiveButton("Yes") { _, _ ->
                            // Remove item from Firestore
                            snapshots.getSnapshot(position).reference.delete()
                                .addOnSuccessListener {
                                    FirebaseUtils.deleteAnnouncement(model.annId)
                                    AndroidUtils.showToast(context, "Announcement deleted successfully.")
                                }
                                .addOnFailureListener { e ->
                                    AndroidUtils.showToast(context, "Failed to delete: ${e.message}")
                                }
                        }
                        .setNegativeButton("No", null)
                        .show()

                    true // Indicate that the long press was handled
                }

            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_recycler_row, parent, false)
        return AnnouncementModelViewHolder(view)
    }
}