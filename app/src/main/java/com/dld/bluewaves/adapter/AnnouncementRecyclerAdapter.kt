package com.dld.bluewaves.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.ImageActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.model.AnnouncementModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.dld.bluewaves.utils.getOtherProfilePicStorageRef
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

@Suppress("DEPRECATION")
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
        val imageBig1: ImageView = itemView.findViewById<View?>(R.id.image_1).findViewById(R.id.image_big)
        val imageProgress1: ProgressBar = itemView.findViewById<View?>(R.id.image_1).findViewById(R.id.progressBar)
        val imageBig2: ImageView = itemView.findViewById<View?>(R.id.image_2).findViewById(R.id.image_big)
        val imageProgress2: ProgressBar = itemView.findViewById<View?>(R.id.image_2).findViewById(R.id.progressBar)
        val imageSmall2: ImageView = itemView.findViewById<View?>(R.id.image_2).findViewById(R.id.image_small)
        val imageProgress3: ProgressBar = itemView.findViewById<View?>(R.id.image_3).findViewById(R.id.progressBar)
        val imageSmall3: ImageView = itemView.findViewById<View?>(R.id.image_3).findViewById(R.id.image_small)
        val extraImagesText: TextView = itemView.findViewById<View?>(R.id.image_3).findViewById(R.id.extra_images_text)
        val imageLayout: View = itemView.findViewById(R.id.image_layout)
    }

    override fun onBindViewHolder(
        holder: AnnouncementModelViewHolder,
        position: Int,
        model: AnnouncementModel
    ) {
        if (position < snapshots.size) {

            FirebaseUtils.getUserAnnouncementReference(model.userId).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val userModel = it.result.toObject(UserModel::class.java)
                    holder.role.text = userModel?.role?.uppercase()
                    holder.displayNameText.text = userModel?.displayName
                    holder.message.text = model.message

                    if (userModel?.profilePic != "") {
                        holder.profilePic.setImageResource(AndroidUtils.selectPicture(userModel?.profilePic!!))
                    } else {
                        getOtherProfilePicStorageRef(model.userId).downloadUrl.addOnCompleteListener { task ->
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

                    // Interactions


                    when (model.imageUrls.size) {
                        0 -> holder.imageLayout.visibility = View.GONE
                        1 -> setupSingleImage(holder, model, position)
                        2 -> setupTwoImages(holder, model, position)
                        3 -> setupThreeImages(holder, model, position)
                        else -> setupMultipleImages(holder, model, position)
                    }

                    FirebaseUtils.currentUserDetails().get().addOnCompleteListener { currentUser ->
                        if (currentUser.isSuccessful) {
                            val role = currentUser.result.getString("role")
                            if (role?.lowercase() in listOf("staff", "admin", "developer")) {
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
                                                    AndroidUtils.showToast(
                                                        context,
                                                        "Announcement deleted successfully."
                                                    )
                                                    FirebaseUtils.getAnnImagePicStorageRef(model.annId)
                                                        .delete()
                                                    notifyItemRemoved(position)
                                                }
                                                .addOnFailureListener { e ->
                                                    AndroidUtils.showToast(
                                                        context,
                                                        "Failed to delete: ${e.message}"
                                                    )
                                                }
                                        }
                                        .setNegativeButton("No", null)
                                        .show()

                                    true // Indicate that the long press was handled
                                }
                            }
                        }
                    }

                }
            }
        } else {
            Log.w("Adapter", "Attempted to bind a view at an invalid position: $position")
        }

    }


    override fun getItemId(position: Int): Long {
        return snapshots.getSnapshot(position).id.hashCode().toLong()
    }
    override fun onViewRecycled(holder: AnnouncementModelViewHolder) {
        super.onViewRecycled(holder)
        // Clear images to avoid old ones sticking to the view when recycled
        holder.imageLayout.visibility = View.GONE
        holder.imageBig1.visibility = View.GONE
        holder.imageBig1.setImageDrawable(null)
        holder.imageProgress1.visibility = View.GONE
        holder.imageBig2.visibility = View.GONE
        holder.imageBig2.setImageDrawable(null)
        holder.imageProgress2.visibility = View.GONE
        holder.imageSmall2.visibility = View.GONE
        holder.imageSmall2.setImageDrawable(null)
        holder.imageProgress3.visibility = View.GONE
        holder.imageSmall3.setImageDrawable(null)
        holder.imageSmall3.visibility = View.GONE
        holder.extraImagesText.visibility = View.GONE
        holder.role.text = ""
        holder.displayNameText.text = ""
        holder.message.text = ""
        holder.lastMessageTime.text = ""
        holder.profilePic.setImageResource(R.drawable.profile_white)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementModelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_recycler_row, parent, false)
        return AnnouncementModelViewHolder(view)
    }

    // Function to navigate to ImageActivity
    private fun navigateToImageActivity(imgList: List<String>, annId: String, initialPosition: Int) {
        val intent = Intent(context, ImageActivity::class.java).apply {
            putStringArrayListExtra("imgList", ArrayList(imgList))
            putExtra("annId", annId)
            putExtra("initialPosition", initialPosition)
        }
        context.startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDataChanged() {
        super.onDataChanged()
        // Notify the RecyclerView that the data has changed or been updated.
        notifyDataSetChanged()
    }


    private fun setupSingleImage(holder: AnnouncementModelViewHolder, model: AnnouncementModel, position: Int) {
        holder.imageLayout.visibility = View.VISIBLE
        holder.imageBig1.visibility = View.VISIBLE

        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[0]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageBig1, holder.imageProgress1)
            }
        }

        holder.imageBig1.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 0)
        }
    }

    private fun setupTwoImages(holder: AnnouncementModelViewHolder, model: AnnouncementModel, position: Int) {
        holder.imageLayout.visibility = View.VISIBLE
        holder.imageBig1.visibility = View.VISIBLE
        holder.imageBig2.visibility = View.VISIBLE

        // Load first image
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[0]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageBig1, holder.imageProgress1)
            }
        }

        // Load second image
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[1]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageBig2, holder.imageProgress2)
            }
        }

        holder.imageBig1.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 0)
        }
        holder.imageBig2.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 1)
        }
    }

    private fun setupThreeImages(holder: AnnouncementModelViewHolder, model: AnnouncementModel, position: Int) {
        holder.imageLayout.visibility = View.VISIBLE
        holder.imageBig1.visibility = View.VISIBLE
        holder.imageSmall2.visibility = View.VISIBLE
        holder.imageSmall3.visibility = View.VISIBLE

        // Load first image
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[0]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageBig1, holder.imageProgress1)
            }
        }

        // Load second image
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[1]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageSmall2, holder.imageProgress2)
            }
        }

        // Load third image
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[2]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful && holder.adapterPosition == position) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageSmall3, holder.imageProgress3)
            }
        }

        holder.imageBig1.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 0)
        }
        holder.imageSmall2.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 1)
        }
        holder.imageSmall3.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 2)
        }
    }

    private fun setupMultipleImages(holder: AnnouncementModelViewHolder, model: AnnouncementModel, position: Int) {

        holder.imageLayout.visibility = View.VISIBLE
        holder.imageBig1.visibility = View.VISIBLE
        holder.imageSmall2.visibility = View.VISIBLE
        holder.imageSmall3.visibility = View.VISIBLE
        holder.extraImagesText.visibility = View.VISIBLE

        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[0]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageBig1, holder.imageProgress1)
            }
        }
        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[1]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageSmall2, holder.imageProgress1)

            }
        }

        FirebaseUtils.getAnnImagePicStorageRef(model.annId).child(model.imageUrls[2]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(context, uri, holder.imageSmall3, holder.imageProgress1)

            }
        }

        holder.imageBig1.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 0)
        }
        holder.imageSmall2.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 1)
        }
        holder.imageSmall3.setOnClickListener {
            navigateToImageActivity(model.imageUrls, model.annId, 2)
        }

        val size = model.imageUrls.size - 3
        val moreText = "+$size more photos"
        holder.extraImagesText.text = moreText


        // Load images similarly as in setupThreeImages, making sure to check `holder.adapterPosition == position` before binding
        // Add logic for the extra images if there are more than 3 images.
    }

}