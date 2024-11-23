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
    }

    fun getUserModelFromIntent(intent: Intent): UserModel {
        val userModel: UserModel = UserModel()
        userModel.displayName = intent.getStringExtra("displayName")!!
        userModel.email = intent.getStringExtra("email")!!
        userModel.userId = intent.getStringExtra("userId")!!
        userModel.fcmToken = intent.getStringExtra("fcmToken")!!
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
}