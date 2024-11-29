package com.dld.bluewaves.adapter

import android.annotation.SuppressLint
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
        val uri = images[holder.bindingAdapterPosition]

        // Load image using Glide
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.image_icon) // Replace with your placeholder
            .centerCrop()
            .into(holder.image)

        // Handle close button click
        holder.closeBtn.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < images.size) {
                images.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = images.size

    override fun getItemId(position: Int): Long {
        return images[position].toString().hashCode().toLong()
    }

    fun addImage(uri: Uri): Int {
        if (!images.contains(uri)) { // Add only if it doesn't already exist
            images.add(uri)
        }
        return images.size - 1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearImages() {
        if (images.isNotEmpty()) {
            images.clear()
            notifyDataSetChanged()
        }
    }

    // Function to retrieve the current list of images
    fun getImages(): List<Uri> {
        return images.toList() // Return a copy to prevent external modifications
    }




}