package com.dld.bluewaves

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dld.bluewaves.adapter.AnnouncementImgRecyclerAdapter
import com.dld.bluewaves.adapter.AnnouncementRecyclerAdapter
import com.dld.bluewaves.databinding.DialogPickImageBinding
import com.dld.bluewaves.databinding.DialogPostAnnouncementBinding
import com.dld.bluewaves.databinding.FragmentAnnouncementBinding
import com.dld.bluewaves.model.AnnouncementModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.storage.storageMetadata
import gun0912.tedimagepicker.builder.TedImagePicker


class AnnouncementFragment : Fragment() {

    private var _binding: FragmentAnnouncementBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var dialogPost: DialogPostAnnouncementBinding
    private lateinit var dialogPick: DialogPickImageBinding
    private lateinit var imgAdapter: AnnouncementImgRecyclerAdapter
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var auth: FirebaseAuth
    private var adapter: AnnouncementRecyclerAdapter? = null
    private lateinit var annModel: AnnouncementModel
    private var lastVisible: DocumentSnapshot? = null
    private var hasMoreData = true

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        annModel = AnnouncementModel()

        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) {
                        selectedImageUri = data.data!!
                        val position = imgAdapter.addImage(selectedImageUri)
                        imgAdapter.notifyItemInserted(position) // Notify only the new item
                    }
                }
            }
        setupRecyclerView()
        setupSwipeRefresh()


        FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
            if (it.isSuccessful) {
                val role = it.result.getString("role")
                if (role?.lowercase() in listOf("staff", "admin", "developer")) {
                    mBinding.postAnnouncement.visibility = View.VISIBLE
                    mBinding.postAnnouncement.setOnClickListener {
                        showPostDialog()
                    }
                }
            }
        }




        return mBinding.root


    }
    private fun setupSwipeRefresh() {
        swipeRefreshLayout = mBinding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            setupRecyclerView() // Reload RecyclerView with fresh data
            swipeRefreshLayout.isRefreshing = false
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
    }

    private fun refreshData() {
        swipeRefreshLayout.isRefreshing = true
        hasMoreData = true

        // Simulate data refresh (Replace with actual refresh logic if needed)
        Handler(Looper.getMainLooper()).postDelayed({
            setupRecyclerView()
            swipeRefreshLayout.isRefreshing = false
        }, 500)
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

        val options = FirestoreRecyclerOptions.Builder<AnnouncementModel>()
            .setQuery(query, AnnouncementModel::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = AnnouncementRecyclerAdapter(options, context as MainActivity)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.adapter = adapter
        mBinding.recyclerView.setHasFixedSize(true)
        adapter?.startListening()

        query.get().addOnSuccessListener { snapshot ->
            lastVisible = snapshot.documents.lastOrNull()
        }

        inProgress(false)
    }


    private fun showPostDialog() {
        dialogPost = DialogPostAnnouncementBinding.inflate(layoutInflater)

        // Initialize the uploading image



        val dialog = AlertDialog.Builder(context as MainActivity)
            .setTitle("Post Announcement")
            .setView(dialogPost.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        imgAdapter = AnnouncementImgRecyclerAdapter(mutableListOf(), context as MainActivity)
        dialogPost.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dialogPost.recyclerView.adapter = imgAdapter

        dialog.setOnDismissListener {
            imgAdapter.clearImages() // Clear images when dialog is dismissed
        }

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                saveButton.isEnabled = false
                var uploadFinish = true
                dialogInProgress(true)
                if (validateMessage(dialogPost)) {
                    val announcementImages = imgAdapter.getImages() // Retrieve images from adapter
                    val annId = FirebaseUtils.allAnnouncementCollectionReference().document().id
                    val announcementImageUrls = mutableListOf<String>()
                    val metadata = storageMetadata {
                        cacheControl = "public, max-age=3600" // Cache for 1 year
                    }

                    // Start uploading images
                    for ((index, announcementImage) in announcementImages.withIndex()) {
                        val imgName = "${annId}_$index"
                        val storageRef =
                            FirebaseUtils.getAnnImagePicStorageRef(annId).child(imgName)
                        storageRef.putFile(announcementImage, metadata)
                            .continueWithTask { task ->
                                if (task.isSuccessful) {
                                    // Get the download URL of the uploaded image
                                    storageRef.downloadUrl
                                } else {
                                    uploadFinish = false
                                    throw task.exception ?: Exception("Image upload failed")

                                }
                            }
                        announcementImageUrls.add(imgName)
                    }


                    // Create and save the announcement
                    if (uploadFinish) {
                        annModel = annModel.copy(
                            annId = annId,
                            userId = auth.currentUser!!.uid,
                            message = dialogPost.messageET.text.toString(),
                            timestamp = Timestamp.now(),
                            imageUrls = announcementImageUrls,
                        )
                        FirebaseUtils.allAnnouncementCollectionReference().document(annModel.annId)
                            .set(annModel)
                            .addOnCompleteListener {
                                dialogInProgress(false)
                                AndroidUtils.showToast(
                                    context as MainActivity,
                                    "Announcement posted"
                                )

                                dialog.dismiss()
                            }
                    }else {
                        // Handle failed uploads
                        dialogInProgress(false)
                        saveButton.isEnabled = true // Re-enable the Save button for retrying
                        AndroidUtils.showToast(
                            context as MainActivity,
                            "Failed to upload some images. Please try again."
                        )
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

        // Handle image upload button
        dialogPost.uploadImageBtn.setOnClickListener {
            uploadClick()
        }





        dialog.show()
    }

    private fun uploadClick(){
        dialogPick = DialogPickImageBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(context as MainActivity)
            .setTitle("Choose...")
            .setView(dialogPick.root)
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        // Set click listeners to close the dialog after performing actions
        val closeDialogAndPerformAction: (() -> Unit) -> View.OnClickListener = { action ->
            View.OnClickListener {
                action()
                dialog.dismiss()
            }
        }

        dialogPick.galleryBtn.setOnClickListener(closeDialogAndPerformAction(::galleryOnly))
        dialogPick.galleryText.setOnClickListener(closeDialogAndPerformAction(::galleryOnly))
        dialogPick.galleryLayout.setOnClickListener(closeDialogAndPerformAction(::galleryOnly))


        dialogPick.cameraBtn.setOnClickListener(closeDialogAndPerformAction(::cameraOnly))
        dialogPick.cameraText.setOnClickListener(closeDialogAndPerformAction(::cameraOnly))
        dialogPick.cameraLayout.setOnClickListener(closeDialogAndPerformAction(::cameraOnly))


        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun galleryOnly() {
        TedImagePicker.with(context as MainActivity)
            .image()
            .startMultiImage { uriList ->
                for (uri in uriList) {
                    val position = imgAdapter.addImage(uri)
                    imgAdapter.notifyItemInserted(position) // Notify only the new item
                }
            }
    }

    private fun cameraOnly(){
        ImagePicker.with(this)
            .cameraOnly()
            .createIntent { intent ->
                imagePickLauncher.launch(intent)
            }
    }


    private fun dialogInProgress(progress: Boolean){
        dialogPost.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }

    private fun inProgress(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }



    // Lifecycle management for FirestoreRecyclerAdapter
    override fun onStart() {
        super.onStart()
        adapter?.startListening() // Start listening once when the fragment starts
    }

    override fun onStop() {
        adapter?.stopListening() // Stop listening when the fragment is no longer in the foreground
        super.onStop()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter?.startListening()
        adapter?.notifyDataSetChanged() // Start listening for changes when the fragment is in the foreground
    }

    override fun onPause() {
        super.onPause()
        adapter?.stopListening() // Stop listening when the fragment is not in the foreground
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding.recyclerView.adapter = null
        adapter?.stopListening()
        _binding = null
    }
}