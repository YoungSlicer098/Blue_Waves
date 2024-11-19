package com.dld.bluewaves.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dld.bluewaves.model.UserModel

object AndroidUtils {

    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun passUserModelAsIntent(intent: Intent, userModel: UserModel){
        intent.putExtra("displayName", userModel.displayName)
        intent.putExtra("email", userModel.email)
        intent.putExtra("userid", userModel.userid)
    }

    fun  getUserModelFromIntent(intent: Intent): UserModel {
        val userModel: UserModel = UserModel
        userModel.displayName = intent.getStringExtra("displayName")!!
        userModel.email = intent.getStringExtra("email")!!
        userModel.userid = intent.getStringExtra("userid")!!
        return userModel

    }
}