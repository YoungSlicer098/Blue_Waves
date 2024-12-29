package com.dld.bluewaves.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.AdminSearchUserActivity
import com.dld.bluewaves.AdminUserEditActivity
import com.dld.bluewaves.ChatActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.SearchUserActivity
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AdminSearchUserRecyclerAdapter(
    options: FirestoreRecyclerOptions<UserModel>,
    private val context: Context,
    private val activity: AdminSearchUserActivity
) : FirestoreRecyclerAdapter<UserModel, AdminSearchUserRecyclerAdapter.UserModelViewHolder>(options) {

    init {
        setHasStableIds(true)
    }

    class UserModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val displayNameText: TextView = itemView.findViewById(R.id.display_name_text)
        val emailText: TextView = itemView.findViewById(R.id.email_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
        val expandCollapseIcon: ImageView = itemView.findViewById(R.id.expand_collapse_icon)
        val collapsibleSection: View = itemView.findViewById(R.id.collapsible_section)
        val mainLayout: View = itemView.findViewById(R.id.main_layout)
        val userIdInput: TextView = itemView.findViewById(R.id.user_id_input)
        val lastSessionInput: TextView = itemView.findViewById(R.id.last_session_input)
        val roleInput: TextView = itemView.findViewById(R.id.role_input)
        val contactNumberInput: TextView = itemView.findViewById(R.id.contactNumber_input)
        val fcmTokenInput: TextView = itemView.findViewById(R.id.fcmToken_input)
        val bannedInput: TextView = itemView.findViewById(R.id.banned_input)
        val editBtn: TextView = itemView.findViewById(R.id.edit_btn)
    }

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int, model: UserModel) {
        // Set username and phone text
        holder.displayNameText.text = model.displayName
        holder.emailText.text = model.email
        holder.userIdInput.text = model.userId
        holder.lastSessionInput.text = model.lastSession.toString()
        holder.roleInput.text = model.role
        holder.contactNumberInput.text = model.contactNumber
        holder.fcmTokenInput.text = model.fcmToken
        if (model.banned == true) {
            holder.bannedInput.text = "Yes"
        } else {
            holder.bannedInput.text = "No"
        }

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

        holder.mainLayout.setOnClickListener {
            if (holder.collapsibleSection.visibility == View.GONE) {
                expandView(holder.collapsibleSection)
                holder.expandCollapseIcon.setImageResource(R.drawable.arrow_down_icon)
            } else {
                collapseView(holder.collapsibleSection)
                holder.expandCollapseIcon.setImageResource(R.drawable.arrow_up_icon)
            }
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
        holder.editBtn.setOnClickListener {
            val intent = Intent(context, AdminUserEditActivity::class.java).apply {
                AndroidUtils.passUserModelAsIntent(this, model)
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
            .inflate(R.layout.admin_search_user_recycler_row, parent, false)
        return UserModelViewHolder(view)
    }

    // Function to expand the view with animation
    private fun expandView(view: View) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = view.measuredHeight

        // Set initial height to 0 and make it visible
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { valueAnimator ->
            view.layoutParams.height = valueAnimator.animatedValue as Int
            view.requestLayout()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 300 // Animation duration in milliseconds
        animator.start()
    }

    // Function to collapse the view with animation
    private fun collapseView(view: View) {
        val initialHeight = view.measuredHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            view.layoutParams.height = valueAnimator.animatedValue as Int
            view.requestLayout()
        }

        animator.addListener(onEnd = {
            view.visibility = View.GONE
        })

        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 300 // Animation duration in milliseconds
        animator.start()
    }
}
