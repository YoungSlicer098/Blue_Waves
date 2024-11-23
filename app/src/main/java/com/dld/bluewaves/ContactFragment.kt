package com.dld.bluewaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dld.bluewaves.adapter.RecentChatRecyclerAdapter
import com.dld.bluewaves.databinding.FragmentContactBinding
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var adapter: RecentChatRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()


        mBinding.searchET.setOnClickListener {
            MainActivity.searchUserActivity(context as MainActivity)
        }

        setupRecyclerView()

        return mBinding.root
    }


    private fun setupRecyclerView() {
        adapter?.stopListening()
        adapter = null
        inProgress(true)

        val query = FirebaseUtils.allChatroomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtils.currentUserId()!!)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .setLifecycleOwner(this) // Automatically starts/stops listening with lifecycle
            .build()

        adapter = RecentChatRecyclerAdapter(options, context as MainActivity)
        mBinding.recyclerView.setLayoutManager(LinearLayoutManager(context))
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()

        inProgress(false)
    }


    private fun inProgress(progress: Boolean) {
        if (progress) {
            mBinding.progressBar.visibility = View.VISIBLE
        } else {
            mBinding.progressBar.visibility = View.GONE
        }
    }


    override fun onStart() {
        super.onStart()
        adapter?.startListening() // Ensure adapter starts listening here
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening() // Properly stop adapter when activity stops
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView() // Ensure setupRecyclerView is called when the fragment resumes
        adapter?.startListening() // Restart listening in case onResume is called without onStart
    }

    override fun onPause() {
        super.onPause()
        adapter?.stopListening() // Avoid memory leaks or inconsistencies
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }
}
