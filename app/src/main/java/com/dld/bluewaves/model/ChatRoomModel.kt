package com.dld.bluewaves.model

import com.google.firebase.Timestamp

data class ChatRoomModel(
    var chatRoomId: String = "",
    var userIds: List<String?> = listOf(),
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String = "",
    var lastMessage:String = ""
)