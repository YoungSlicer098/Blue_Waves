package com.dld.bluewaves.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.ChatActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.SearchUserActivity
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class SearchUserRecyclerAdapter(
    options: FirestoreRecyclerOptions<UserModel>,
    private val context: Context,
    private val activity: SearchUserActivity
) : FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder>(options) {

    init {
        setHasStableIds(true)
    }

    class UserModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val displayNameText: TextView = itemView.findViewById(R.id.display_name_text)
        val emailText: TextView = itemView.findViewById(R.id.email_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
    }

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int, model: UserModel) {
        // Set username and phone text
        holder.displayNameText.text = model.displayName
        holder.emailText.text = model.email

        if (model.profilePic != "") {
            holder.profilePic.setImageResource(AndroidUtils.selectPicture(model.profilePic))
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

        // Add "(Me)" if the user ID matches the current user
        if (model.userId == FirebaseUtils.currentUserId()) {
            holder.displayNameText.text = "${model.displayName} (Me)"
            holder.itemView.isClickable = false
        }
//
//        // Reset profile picture to placeholder
//        holder.profilePic.setImageResource(R.drawable.default_profile_pic)
//
//        // Load profile picture asynchronously
//        FirebaseUtil.getOtherProfilePicStorageRef(model.userId).downloadUrl
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val uri = task.result
//                    Glide.with(context)
//                        .load(uri)
//                        .placeholder(R.drawable.default_profile_pic) // Optional: placeholder
//                        .error(R.drawable.default_profile_pic) // Optional: error fallback
//                        .into(holder.profilePic)
//                } else {
//                    Log.e("SearchUserAdapter", "Failed to load profile picture", task.exception)
//                }
//            }
//
//        // Set click listener to navigate to ChatActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                AndroidUtils.passUserModelAsIntent(this, model)
                this.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            activity.finish()
        }
    }

    override fun getItemId(position: Int): Long {
        return snapshots.getSnapshot(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_user_recycler_row, parent, false)
        return UserModelViewHolder(view)
    }
}