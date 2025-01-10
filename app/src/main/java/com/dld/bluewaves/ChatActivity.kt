package com.dld.bluewaves

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.adapter.ChatRecyclerAdapter
import com.dld.bluewaves.databinding.ActivityChatBinding
import com.dld.bluewaves.model.ChatMessageModel
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.dld.bluewaves.utils.FirebaseUtils.getOtherProfilePicStorageRef
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityChatBinding
    private lateinit var otherUser: UserModel
    private lateinit var chatroomId: String
    private var chatRoomModel = ChatRoomModel()
    private lateinit var adapter: ChatRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        otherUser = AndroidUtils.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtils.getChatroomId(FirebaseUtils.currentUserId()!!, otherUser.userId)

        if (otherUser.profilePic != "") {
            mBinding.profilePicLayout.profilePicImageView.setImageResource(AndroidUtils.selectPicture(otherUser.profilePic))
        }else {
            getOtherProfilePicStorageRef(otherUser.userId).downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    val uri: Uri = it.result
                    AndroidUtils.setProfilePic(
                        this,
                        uri,
                        mBinding.profilePicLayout.profilePicImageView
                    )
                }else{
                    mBinding.profilePicLayout.profilePicImageView.setImageResource(R.drawable.profile_white)
                }
            }
        }

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        mBinding.otherDisplayName.text = otherUser.displayName

        mBinding.messageSendBtn.setOnClickListener {
            val message = mBinding.chatMessageInput.text.toString().trim()
            if (message.isEmpty()) {
                return@setOnClickListener
            } else {
                sendMessageToUser(message)
            }
        }

        checkUserIds()
        getOrCreateChatroomModel()
        setupChatRecyclerView()

    }

    private fun checkUserIds() {
        if (chatRoomModel.userIds.size == 2) {
            if (chatRoomModel.userIds[0] == FirebaseUtils.currentUserId() && chatRoomModel.userIds[1] == otherUser.userId) {
                return
            } else if (chatRoomModel.userIds[0] == otherUser.userId && chatRoomModel.userIds[1] == FirebaseUtils.currentUserId()) {
                return
            } else {
                chatRoomModel.userIds = listOf(FirebaseUtils.currentUserId(), otherUser.userId)
                FirebaseUtils.getChatroomReference(chatroomId).update("userIds",chatRoomModel.userIds)
            }
        } else {
            chatRoomModel.userIds = listOf(FirebaseUtils.currentUserId(), otherUser.userId)
            FirebaseUtils.getChatroomReference(chatroomId).update("userIds",chatRoomModel.userIds)
        }
    }

    private fun setupChatRecyclerView() {


        val query: Query = FirebaseUtils.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatMessageModel> =
            FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java).build()

        adapter = ChatRecyclerAdapter(options, applicationContext, otherUser.profilePic)
        val manager: LinearLayoutManager = LinearLayoutManager(this)
        manager.setReverseLayout(true)
        mBinding.recyclerView.setLayoutManager(manager)
        mBinding.recyclerView.adapter = adapter
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                mBinding.recyclerView.smoothScrollToPosition(0)
            }
        })
    }

    private fun sendMessageToUser(message: String) {

        chatRoomModel.lastMessageTimestamp = Timestamp.now()
        chatRoomModel.lastMessageSenderId = FirebaseUtils.currentUserId()!!
        chatRoomModel.lastMessage = message

        FirebaseUtils.getChatroomReference(chatroomId).set(chatRoomModel)

        val chatMessageModel = ChatMessageModel(
            message = message,
            senderId = FirebaseUtils.currentUserId()!!,
            timestamp = Timestamp.now()
        )

        FirebaseUtils.getChatroomMessageReference(chatroomId).add(chatMessageModel)
            .addOnCompleteListener(
                OnCompleteListener<DocumentReference> { task ->
                    if (task.isSuccessful) {
                        mBinding.chatMessageInput.setText("")
//                        sendNotification(message)
                    } else {
                        AndroidUtils.showToast(this, "Error sending message")
                    }
                }
            ).addOnFailureListener {
                AndroidUtils.showToast(this, "Error sending message")
            }
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtils.getChatroomReference(chatroomId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    // Check if the document exists
                    if (result != null && result.exists()) {
                        // Safely map the document data to the model
                        chatRoomModel =
                            result.toObject(ChatRoomModel::class.java) ?: ChatRoomModel()
                    } else {
                        // If the chatroom doesn't exist, create a new one
                        chatRoomModel = ChatRoomModel(
                            chatRoomId = chatroomId,
                            userIds = listOf(FirebaseUtils.currentUserId(), otherUser.userId),
                            lastMessageTimestamp = Timestamp.now(),
                            lastMessageSenderId = ""
                        )
                        // Save the new chatroom model to Firestore
                        FirebaseUtils.getChatroomReference(chatroomId).set(chatRoomModel)
                    }
                } else {
                    // Handle any error during fetching the document
                    task.exception?.let {
                        // Log or handle the error appropriately
                        println("Error fetching chatroom: ${it.message}")
                    }
                }
            }
    }
//
//    private fun sendNotification(message: String) {
//        FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
//            if (it.isSuccessful) {
//                val currentUser: UserModel = it.result.toObject(UserModel::class.java)!!
//                try {
//                    val jsonObject = JSONObject()
//
//                    // Notification section
//                    val notificationObj = JSONObject()
//                    notificationObj.put("title", currentUser.displayName)
//                    notificationObj.put("body", message)
//
//                    // Data section (custom data)
//                    val dataObj = JSONObject()
//                    dataObj.put("userId", currentUser.userId)
//
//                    // Message payload
//                    val messageObj = JSONObject()
//                    messageObj.put(
//                        "token",
//                        otherUser.fcmToken
//                    ) // Specify the FCM token of the recipient
//                    messageObj.put("notification", notificationObj)
//                    messageObj.put("data", dataObj)
//
//                    jsonObject.put("message", messageObj)
//
//                    callAPI(jsonObject) // Call the API with the structured payload
//                } catch (e: Exception) {
//                    e.printStackTrace() // Log the error for debugging purposes
//                }
//            }
//        }
//    }
//
//    private fun callAPI(jsonObject: JSONObject) {
//        val client = OkHttpClient.Builder()
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .build()
//
//        val JSON = "application/json".toMediaTypeOrNull()
//        val url = "https://fcm.googleapis.com/v1/projects/bluewaves-dld/messages:send"
//
//        val bearerToken = getAccessToken(this)
//
//        val body: RequestBody = RequestBody.create(JSON, jsonObject.toString())
//        val request: Request = Request.Builder()
//            .url(url)
//            .post(body)
//            .header("Authorization", "Bearer $bearerToken")
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: okhttp3.Call, e: IOException) {
//                e.printStackTrace()
//                println("Failed to send notification: ${e.message}")
//            }
//
//            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
//                response.use {
//                    if (response.isSuccessful) {
//                        println("Notification sent successfully")
//                    } else {
//                        val errorBody = response.body?.string()
//                        println("Error sending notification: $errorBody")
//                    }
//                }
//            }
//        })
//    }

}