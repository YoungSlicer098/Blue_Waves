package com.dld.bluewaves.model

import com.google.firebase.Timestamp

data class UserModel(
    var userId: String = "",
    var lastSession: Timestamp? = null,
    var displayName: String = "",
    var displayNameLowercase: String = "",
    var email: String = "",
    var profilePic: String = "",
    var role: String = "",
    var searchKeywords: List<String>? = null,
    var fcmToken: String = "",
    var contactNumber: String = "",
    var banned: Boolean = false,
)