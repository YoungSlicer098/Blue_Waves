package com.dld.bluewaves.model

import com.google.firebase.Timestamp

data class AnnouncementModel(
    var annId: String = "",
    var userId: String = "",
    var message: String = "",
    var timestamp: Timestamp? = null,
    var imageUrls : List<String>  = listOf(),
)