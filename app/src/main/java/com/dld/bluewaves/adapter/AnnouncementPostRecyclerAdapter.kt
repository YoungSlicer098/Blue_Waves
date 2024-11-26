package com.dld.bluewaves.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dld.bluewaves.R


class AnnouncementImgRecyclerAdapter(
    private val images: MutableList<Uri>,
    private val context: Context
) : RecyclerView.Adapter<AnnouncementImgRecyclerAdapter.AnnouncementImgViewHolder>() {

    init {
        setHasStableIds(true)
    }

    class AnnouncementImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val closeBtn: ImageView = itemView.findViewById(R.id.closeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementImgViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_img_recycler_row, parent, false)
        return AnnouncementImgViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementImgViewHolder, position: Int) {
        val uri = images[position]

        // Load image using Glide
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.image_icon) // Replace with your placeholder
            .centerCrop()
            .into(holder.image)

        // Handle close button click
        holder.closeBtn.setOnClickListener {
            images.removeAt(position) // Remove the image
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, images.size) // Notify changes for proper update
        }
    }

    override fun getItemCount(): Int = images.size

    override fun getItemId(position: Int): Long {
        return images[position].hashCode().toLong()
    }

    // Function to add a new image
    fun addImage(uri: Uri) {
        images.add(uri)
        notifyItemInserted(images.size - 1)
    }

    // Function to retrieve the current list of images
    fun getImages(): List<Uri> {
        return images.toList() // Return a copy to prevent external modifications
    }
}