package com.dld.bluewaves

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.adapter.ChatRecyclerAdapter
import com.dld.bluewaves.adapter.SearchUserRecyclerAdapter
import com.dld.bluewaves.databinding.ActivityChatBinding
import com.dld.bluewaves.model.ChatMessageModel
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

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
        getOrCreateChatroomModel()
        setupChatRecyclerView()

    }

    private fun setupChatRecyclerView() {


        val query: Query = FirebaseUtils.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatMessageModel> = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()

        adapter = ChatRecyclerAdapter(options, applicationContext)
        val manager: LinearLayoutManager = LinearLayoutManager(this)
        manager.setReverseLayout(true)
        mBinding.recyclerView.setLayoutManager(manager)
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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

        FirebaseUtils.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(
            OnCompleteListener<DocumentReference>(){
                task ->
                if(task.isSuccessful){
                    mBinding.chatMessageInput.setText("")
                }else{
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
                        chatRoomModel = result.toObject(ChatRoomModel::class.java) ?: ChatRoomModel()
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
}