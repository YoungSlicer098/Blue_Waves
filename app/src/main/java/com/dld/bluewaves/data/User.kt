package com.dld.bluewaves.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phoneNumber: String
)
