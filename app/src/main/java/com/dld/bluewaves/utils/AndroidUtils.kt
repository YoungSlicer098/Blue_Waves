@file:Suppress("DEPRECATION")

package com.dld.bluewaves.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.dld.bluewaves.R
import com.dld.bluewaves.model.UserModel
import com.github.chrisbanes.photoview.PhotoView

object AndroidUtils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun passUserModelAsIntent(intent: Intent, userModel: UserModel) {
        intent.putExtra("displayName", userModel.displayName)
        intent.putExtra("email", userModel.email)
        intent.putExtra("userId", userModel.userId)
        intent.putExtra("fcmToken", userModel.fcmToken)
        intent.putExtra("profilePic", userModel.profilePic)
    }


    fun passAllUserModelAsIntent(intent: Intent, userModel: UserModel) {
        intent.putExtra("displayName", userModel.displayName)
        intent.putExtra("email", userModel.email)
        intent.putExtra("userId", userModel.userId)
        intent.putExtra("fcmToken", userModel.fcmToken)
        intent.putExtra("profilePic", userModel.profilePic)
        intent.putExtra("contactNumber", userModel.contactNumber)
        intent.putExtra("lastSession", userModel.lastSession)
        intent.putExtra("banned", userModel.banned)
        intent.putExtra("role", userModel.role)

    }

    fun getUserModelFromIntent(intent: Intent): UserModel {
        val userModel: UserModel = UserModel()
        userModel.displayName = intent.getStringExtra("displayName")!!
        userModel.email = intent.getStringExtra("email")!!
        userModel.userId = intent.getStringExtra("userId")!!
        userModel.fcmToken = intent.getStringExtra("fcmToken")!!
        userModel.profilePic = intent.getStringExtra("profilePic")!!
        return userModel

    }


    fun getAllUserModelFromIntent(intent: Intent): UserModel {
        val userModel: UserModel = UserModel()
        userModel.displayName = intent.getStringExtra("displayName")!!
        userModel.email = intent.getStringExtra("email")!!
        userModel.userId = intent.getStringExtra("userId")!!
        userModel.fcmToken = intent.getStringExtra("fcmToken")!!
        userModel.profilePic = intent.getStringExtra("profilePic")!!
        userModel.contactNumber = intent.getStringExtra("contactNumber")!!
        userModel.lastSession = intent.getParcelableExtra("lastSession")!!
        userModel.banned = intent.getBooleanExtra("banned", false)
        userModel.role = intent.getStringExtra("role")!!
        return userModel

    }

    //    fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView){
//        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView)
//    }
    fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView) {
        Glide.with(context)
            .load(imageUri)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Explicitly cache all versions
                    .placeholder(R.drawable.profile_white) // Placeholder for loading state
            )
            .into(imageView)
    }

    fun selectPicture(select: String): Int {
        return when (select) {
            "pfp1" -> R.drawable.pfp1
            "pfp2" -> R.drawable.pfp2
            "pfp3" -> R.drawable.pfp3
            "pfp4" -> R.drawable.pfp4
            "pfp5" -> R.drawable.pfp5
            "pfp6" -> R.drawable.pfp6
            else -> R.drawable.profile_white
        }
    }



    fun loadPicAnn(context: Context, imageUri: Uri, imageView: ImageView, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE  // Show progress bar before image loading

        Glide.with(context)
            .load(imageUri)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image_icon)
                .error(R.drawable.image_error_icon)
            )
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    progressBar.visibility = View.GONE  // Hide progress bar after image is loaded
                    imageView.setImageDrawable(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    progressBar.visibility = View.GONE  // Hide progress bar if loading fails
                    retryLoadImage(context, imageUri, imageView, progressBar, 1)
                }
            })
    }

    fun loadPicAnn(context: Context, imageUri: Uri, imageView: PhotoView, progressBar: ProgressBar){
        progressBar.visibility = View.VISIBLE  // Show progress bar before image loading

        Glide.with(context)
            .load(imageUri)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image_icon)
                .error(R.drawable.image_error_icon)
            )
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    progressBar.visibility = View.GONE  // Hide progress bar after image is loaded
                    imageView.setImageDrawable(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    progressBar.visibility = View.GONE  // Hide progress bar if loading fails
                    retryLoadImage(context, imageUri, imageView, progressBar, 1)
                }
            })
    }


    fun retryLoadImage(context: Context, imageUri: Uri, imageView: ImageView, progressBar: ProgressBar, attemptCount: Int) {
        if (attemptCount <= 3) {  // Max retry attempts (e.g., 3)
            Handler(Looper.getMainLooper()).postDelayed({
                loadPicAnn(context, imageUri, imageView, progressBar)
            }, 1000) // Retry after 1 second
        } else {
            // After 3 failed attempts, show an error message or default image
            imageView.setImageResource(R.drawable.image_error_icon)
        }
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }


    fun generateSearchKeywords(displayName: String): List<String> {
        val keywords = mutableSetOf<String>()
        val words = displayName.lowercase().split(" ")

        // Add full name and individual words
        keywords.add(displayName.lowercase())
        keywords.addAll(words)

        // Add substrings for each word
        words.forEach { word ->
            for (i in 1..word.length) {
                for (j in 0..word.length - i) {
                    keywords.add(word.substring(j, j + i))
                }
            }
        }

        return keywords.toList()
    }

}