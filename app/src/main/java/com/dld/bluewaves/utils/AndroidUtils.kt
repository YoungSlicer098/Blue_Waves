package com.dld.bluewaves.utils

import android.content.Context
import android.widget.Toast

object AndroidUtils {

    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}