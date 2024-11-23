package com.dld.bluewaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dld.bluewaves.adapter.AnnouncementRecyclerAdapter
import com.dld.bluewaves.adapter.RecentChatRecyclerAdapter
import com.dld.bluewaves.databinding.DialogPostAnnouncementBinding
import com.dld.bluewaves.databinding.FragmentAnnouncementBinding
import com.dld.bluewaves.model.AnnouncementModel
import com.dld.bluewaves.model.ChatRoomModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class AnnouncementFragment : Fragment() {

    private var _binding: FragmentAnnouncementBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var dialogPost: DialogPostAnnouncementBinding
    private lateinit var auth: FirebaseAuth
    private var adapter: AnnouncementRecyclerAdapter? = null
    private lateinit var annModel: AnnouncementModel
    private var times = 0
    private var hasMoreData = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        annModel = AnnouncementModel()

        mBinding.postAnnouncement.setOnClickListener {
            dialogPost = DialogPostAnnouncementBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(context as MainActivity)
                .setTitle("Post Announcement")
                .setView(dialogPost.root)
                .setPositiveButton("Save", null) // Initially null to handle manually
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Custom behavior for the Save button
            dialog.setOnShowListener {
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    inProgress(true)
                    if (validateMessage(dialogPost)) {
                        annModel = annModel.copy(
                            annId = FirebaseUtils.allAnnouncementCollectionReference()
                                .document().id,
                            userId = auth.currentUser!!.uid,
                            message = dialogPost.messageET.text.toString(),
                            timestamp = Timestamp.now()
                        )
                        FirebaseUtils.allAnnouncementCollectionReference().document(annModel.annId)
                            .set(annModel).addOnCompleteListener {
                                inProgress(false)
                                AndroidUtils.showToast(
                                    context as MainActivity,
                                    "Announcement posted"
                                )
                                dialog.dismiss()
                            }
                    }
                }
            }

            dialogPost.messageET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    dialogPost.messageTil.error = null
                    dialogPost.messageTil.isErrorEnabled = false
                } else {
                    validateMessage(dialogPost)
                }
            }


            dialog.show()
        }


        setupRecyclerView()

        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Don't load more data if there's no more data to load
                if (!hasMoreData) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                // Check if the user has scrolled to the bottom of the list
                if ((visibleItemCount + pastVisibleItems) >= totalItemCount ||
                    layoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
                    // Load more data if at the bottom and there is more data
                    loadMoreData()
                }
            }
        })



        return mBinding.root


    }


    private fun validateMessage(dialogPost: DialogPostAnnouncementBinding): Boolean {
        val value = dialogPost.messageET.text.toString()
        if (value.isEmpty()) {
            dialogPost.messageTil.error = "Field cannot be empty"
            dialogPost.messageTil.isErrorEnabled = true
        } else if (value.length < 10) {
            dialogPost.messageTil.error = "Message is too short"
            dialogPost.messageTil.isErrorEnabled = true

        } else {
            dialogPost.messageTil.error = null
            dialogPost.messageTil.isErrorEnabled = false
        }
        return dialogPost.messageTil.error == null
    }


    private fun setupRecyclerView() {
        inProgress(true)

        val query = FirebaseUtils.allAnnouncementCollectionReference()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)

        val options = FirestoreRecyclerOptions.Builder<AnnouncementModel>()
            .setQuery(query, AnnouncementModel::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = AnnouncementRecyclerAdapter(options, context as MainActivity)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()

        inProgress(false)

    }

    private fun loadMoreData() {
        if (!hasMoreData) return // Stop if no more data is available

        inProgress(true) // Show progress indicator

        val currentSnapshots = adapter?.snapshots ?: return inProgress(false) // Ensure snapshots are not null

        if (currentSnapshots.isEmpty()) {
            // If there are no items yet, stop loading more
            hasMoreData = false
            inProgress(false)
            return
        }

        val lastVisibleItem = (mBinding.recyclerView.layoutManager as LinearLayoutManager)
            .findLastVisibleItemPosition()

        if (lastVisibleItem < 0 || lastVisibleItem >= currentSnapshots.size) {
            // If the last item index is invalid, stop loading
            inProgress(false)
            return
        }

        val lastVisibleDocument = currentSnapshots.getSnapshot(lastVisibleItem)

        // Prepare the query for the next batch of data
        val nextQuery = FirebaseUtils.allAnnouncementCollectionReference()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisibleDocument) // Start after the last visible document
            .limit(20) // Limit the results for pagination

        nextQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result?.isEmpty == true) {
                    // No more data available
                    hasMoreData = false
                } else {
                    // Update the adapter with the new query results
                    val options = FirestoreRecyclerOptions.Builder<AnnouncementModel>()
                        .setQuery(nextQuery, AnnouncementModel::class.java)
                        .setLifecycleOwner(viewLifecycleOwner)
                        .build()

                    mBinding.recyclerView.post {
                        adapter?.updateOptions(options)
                    }
                }
            } else {
                // Handle errors during data loading
                AndroidUtils.showToast(context as MainActivity, "Error loading more data: ${task.exception?.message}")
            }
            inProgress(false) // Hide progress indicator
        }
    }


    private fun inProgress(progress: Boolean) {
        if (progress) {
            mBinding.progressingBar.visibility = View.VISIBLE
        } else {
            mBinding.progressingBar.visibility = View.GONE
        }
    }

//
//    // Lifecycle management for FirestoreRecyclerAdapter
//    override fun onStart() {
//        super.onStart()
//        if (adapter != null) {
//            adapter?.startListening() // Start listening once when the fragment starts
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (adapter != null) {
//            adapter?.stopListening() // Stop listening when the fragment is no longer in the foreground
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (adapter != null) {
//            adapter?.startListening() // Ensure adapter starts listening here
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (adapter != null) {
//            adapter?.stopListening() // Properly stop listening when the fragment is paused
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        if (adapter != null) {
//            adapter?.stopListening() // Clean up to avoid memory leaks
//        }
//        _binding = null // Clear binding reference to prevent memory leaks
//    }
}