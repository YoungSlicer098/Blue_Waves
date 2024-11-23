package com.dld.bluewaves.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.dld.bluewaves.R
import com.dld.bluewaves.model.UserModel

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

    fun getUserModelFromIntent(intent: Intent): UserModel {
        val userModel: UserModel = UserModel()
        userModel.displayName = intent.getStringExtra("displayName")!!
        userModel.email = intent.getStringExtra("email")!!
        userModel.userId = intent.getStringExtra("userId")!!
        userModel.fcmToken = intent.getStringExtra("fcmToken")!!
        userModel.profilePic = intent.getStringExtra("profilePic")!!
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
}