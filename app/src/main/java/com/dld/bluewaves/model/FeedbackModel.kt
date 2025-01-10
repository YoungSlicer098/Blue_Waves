package com.dld.bluewaves.model

import com.google.firebase.Timestamp

data class FeedbackModel(
    var fbId: String = "",
    var userId: String = "",
    var message: String = "",
    var timestamp: Timestamp? = null,
)