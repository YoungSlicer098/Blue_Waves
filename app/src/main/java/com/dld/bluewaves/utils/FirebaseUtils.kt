package com.dld.bluewaves.utils

import com.dld.bluewaves.model.AnnouncementModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Locale

object FirebaseUtils {


    //Users

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }


    fun createUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId() ?: throw IllegalStateException("User not logged in"))
    }

    fun currentUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId() ?: throw IllegalStateException("User not logged in"))
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun allUserCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun sameDisplayNameVerify(displayName: String): Boolean {
        var name: String? = null
        allUserCollectionReference().whereEqualTo("displayName", displayName).get().addOnCompleteListener{
            if (it.isSuccessful) name = it.result.documents[0].getString("displayName")
            else name = null
        }
        return name != null
    }

    fun sameEmailVerify(email: String): Boolean {
        var value: String? = null
        allUserCollectionReference().whereEqualTo("email", email).get().addOnCompleteListener{
            if (it.isSuccessful) value = it.result.documents[0].getString("email")
            else value = null
        }
        return value != null
    }

    fun sameContactNumberVerify(contactNumber: String): Boolean {
        var value: String? = null
        allUserCollectionReference().whereEqualTo("contactNumber", contactNumber).get().addOnCompleteListener{
            if (it.isSuccessful) value = it.result.documents[0].getString("contactNumber")
            else value = null
        }
        return value != null
    }


    // Chatroom

    // Collect ChatroomID
    fun getChatroomReference(chatroomId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("chatrooms")
            .document(chatroomId)
    }

    // Collect Chats from Chatroom
    fun getChatroomMessageReference(chatroomId: String): CollectionReference {
        return getChatroomReference(chatroomId).collection("chats")
    }

    // Creating Chatroom ID
    fun getChatroomId(userId1: String, userId2: String): String {
        return if (userId1.hashCode() < userId2.hashCode()) {
            userId1 + "_" + userId2
        } else {
            userId2 + "_" + userId1
        }
    }

    // Collect All Chatroom Reference
    fun allChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }






    fun getOtherUserFromChatroom(userIds: List<String?>): DocumentReference {
        val currentUserId = currentUserId() ?: throw IllegalStateException("User not logged in")
        val otherUserId = if (userIds[0] == currentUserId) userIds[1] else userIds[0]
        return allUserCollectionReference().document(otherUserId!!)
    }


    fun getCurrentProfilePicStorageRef(): StorageReference {
        val userId = currentUserId() ?: throw IllegalStateException("User not logged in")
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(userId)
    }

    //
//    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
//        return FirebaseStorage.getInstance().reference
//            .child("profile_pic")
//            .child(otherUserId)
//    }

    // Getting the other user's data from the profile pic storage
    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference
            .child("profile_pic")
            .child(otherUserId)
    }

    //Announcements

    // Collect a certain announcement
    fun getAnnouncementReference(announcementId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("announcements").document(announcementId)
    }

    // Collect user data for announcement
    fun getUserAnnouncementReference(userId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
    }

    // Delete Announcement
    fun deleteAnnouncement(annId: String): Task<Void> {
        return getAnnouncementReference(annId).delete()
    }

    // Collect all announcements
    fun allAnnouncementCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("announcements")
    }

    // Create or Post an Announcement
    fun createAnnouncement(model: AnnouncementModel): Task<DocumentReference> {
        return allAnnouncementCollectionReference().add(model)
    }

    fun getAnnImagePicStorageRef(image: String): StorageReference {
        return FirebaseStorage.getInstance().reference
            .child("annPics")
            .child(image)
    }
}
