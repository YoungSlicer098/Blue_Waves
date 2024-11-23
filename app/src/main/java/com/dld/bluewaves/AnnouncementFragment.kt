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
                            annId = FirebaseUtils.allAnnouncementCollectionReference().document().id,
                            userId = auth.currentUser!!.uid,
                            message = dialogPost.messageET.text.toString(),
                            timestamp = Timestamp.now()
                        )
                        FirebaseUtils.allAnnouncementCollectionReference().document(annModel.annId)
                            .set(annModel).addOnCompleteListener{
                                inProgress(false)
                                AndroidUtils.showToast(context as MainActivity, "Announcement posted")
                                dialog.dismiss()
                            }
                    }
                }
            }

            dialogPost.messageET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    dialogPost.messageTil.error = null
                    dialogPost.messageTil.isErrorEnabled = false
                }else{
                    validateMessage(dialogPost)
                }
            }


            dialog.show()
        }


        setupRecyclerView()

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

        }
        else {
            dialogPost.messageTil.error = null
            dialogPost.messageTil.isErrorEnabled = false
        }
        return dialogPost.messageTil.error == null
    }
    private fun setupRecyclerView() {
        adapter?.stopListening()
        adapter = null

        inProgress(true) // Show the progress bar while setting up

        val query = FirebaseUtils.allAnnouncementCollectionReference()
            .orderBy("timestamp", Query.Direction.DESCENDING) // Latest first

        val options = FirestoreRecyclerOptions.Builder<AnnouncementModel>()
            .setQuery(query, AnnouncementModel::class.java)
            .setLifecycleOwner(this) // Automatically starts/stops listening with lifecycle
            .build()

        adapter = AnnouncementRecyclerAdapter(options, context as MainActivity)

        // Ensure the RecyclerView is initialized before data is loaded
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.adapter = adapter

        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                inProgress(false) // Hide the progress bar once data is loaded
            }
        })
        inProgress(false)
        adapter?.startListening()
    }

    private fun inProgress(progress: Boolean) {
        if (progress) {
            mBinding.progressingBar.visibility = View.VISIBLE
        } else {
            mBinding.progressingBar.visibility = View.GONE
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