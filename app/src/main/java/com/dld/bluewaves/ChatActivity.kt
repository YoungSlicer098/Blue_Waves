package com.dld.bluewaves

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivityChatBinding
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.firebase.Timestamp

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityChatBinding
    private lateinit var otherUser: UserModel
    private lateinit var chatroomId: String
    private var chatRoomModel = ChatRoomModel()

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

    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtils.getChatroomReference(chatroomId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatRoomModel = task.result?.toObject(ChatRoomModel::class.java)!!
                    if (chatRoomModel == null) {
                        chatRoomModel = ChatRoomModel(
                        chatRoomId = chatroomId,
                        userIds = listOf(FirebaseUtils.currentUserId(), otherUser.userId),
                        lastMessageTimestamp = Timestamp.now(),
                        lastMessageSenderId = ""
                        )
                        FirebaseUtils.getChatroomReference(chatroomId).set(chatRoomModel)
                    }
                }
            }
    }
}