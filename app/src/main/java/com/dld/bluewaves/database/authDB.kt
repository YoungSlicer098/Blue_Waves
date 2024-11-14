package com.dld.bluewaves.database

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class authDB {
    private val db = Firebase.database.reference

    fun registerUser(auth: FirebaseAuth, email: String, displayName: String, profilePicUrl: String) {

        val user = auth.currentUser
        user?.let {
            // Get UID and other user information
            val uid = user.uid
            val databaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)

            // Create a user data map or a data class instance
            val userData = mapOf(
                "authID" to uid,
                "email" to email,
                "displayName" to displayName,
                "profilePic" to profilePicUrl,
                "dateCreated" to System.currentTimeMillis(),
                "lastLogged" to System.currentTimeMillis(),
                "role" to "customer"
            )

            // Save user data to the database
            databaseRef.setValue(userData)
                .addOnSuccessListener {
                    // User data saved successfully
                }
                .addOnFailureListener { e ->
                    // Handle the error
                }
        }
    }
}

}