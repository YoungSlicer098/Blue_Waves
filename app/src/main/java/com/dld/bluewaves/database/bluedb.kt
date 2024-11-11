package com.dld.bluewaves.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class bluedb(){

    fun registerUser(email: String, password: String, displayName: String, profilePicUrl: String) {
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the current user and UID
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
                } else {
                    // Handle the registration error
                }
            }
    }
}
