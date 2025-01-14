package com.dld.bluewaves

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dld.bluewaves.adapter.RecentChatRecyclerAdapter
import com.dld.bluewaves.databinding.FragmentContactBinding
import com.dld.bluewaves.model.AnnouncementModel
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth
    private var adapter: RecentChatRecyclerAdapter? = null
    private var times = 0
    private var hasMoreData = true

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
        setupSwipeRefresh()


        return mBinding.root
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout = mBinding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
    }

    private fun refreshData() {
        swipeRefreshLayout.isRefreshing = true

        // Simulate data refresh (Replace with actual refresh logic if needed)
        Handler(Looper.getMainLooper()).postDelayed({
            setupRecyclerView()
            swipeRefreshLayout.isRefreshing = false
        }, 2000)
    }

    private fun setupRecyclerView() {
        inProgress(true)

        val query = FirebaseUtils.allChatroomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtils.currentUserId()!!)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .setLifecycleOwner(viewLifecycleOwner) // Automatically starts/stops listening with lifecycle
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

    // Start adapter listening in onStart
    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    // Stop adapter listening in onStop
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged() // Ensure dataset is consistent
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding.recyclerView.adapter = null // Clear adapter to prevent memory leaks
        _binding = null // Clear binding reference
    }
}
