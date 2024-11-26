package com.dld.bluewaves.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.R
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.github.chrisbanes.photoview.PhotoView
import java.util.ArrayList

class ImagePagerAdapter(private val images: ArrayList<String>, private val annId: String) : RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: PhotoView = view.findViewById(R.id.image_view)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        FirebaseUtils.getAnnImagePicStorageRef(annId).child(images[position]).downloadUrl.addOnCompleteListener { img ->
            if (img.isSuccessful) {
                val uri: Uri = img.result
                AndroidUtils.loadPicAnn(holder.imageView.context, uri, holder.imageView, holder.progressBar)
            }
        }
    }

    override fun getItemCount() = images.size
}