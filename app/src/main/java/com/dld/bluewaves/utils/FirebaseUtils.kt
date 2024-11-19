package com.dld.bluewaves.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object FirebaseUtils {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }


    fun createUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId() ?: throw IllegalStateException("User not logged in"))
    }

    fun allUserCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun getChatroomReference(chatroomId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("chatrooms")
            .document(chatroomId)
    }

    fun getChatroomMessageReference(chatroomId: String): CollectionReference {
        return getChatroomReference(chatroomId).collection("chats")
    }

    fun getChatroomId(userId1: String, userId2: String): String {
        return if (userId1.hashCode() < userId2.hashCode()) {
            userId1+"_"+userId2
        } else {
            userId2+"_"+userId1
        }
    }

    fun allChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }

    fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference {
        val currentUserId = currentUserId() ?: throw IllegalStateException("User not logged in")
        val otherUserId = if (userIds[0] == currentUserId) userIds[1] else userIds[0]
        return allUserCollectionReference().document(otherUserId)
    }

//    fun timestampToString(timestamp: Timestamp): String {
//        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//        return dateFormat.format(timestamp.toDate())
//    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference {
        val userId = currentUserId() ?: throw IllegalStateException("User not logged in")
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(userId)
    }

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(otherUserId)
    }
}
