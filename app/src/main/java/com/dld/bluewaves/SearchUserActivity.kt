package com.dld.bluewaves

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dld.bluewaves.adapter.SearchUserRecyclerAdapter
import com.dld.bluewaves.databinding.ActivitySearchUserBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query

@Suppress("DEPRECATION")
class SearchUserActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySearchUserBinding
    private var adapter: SearchUserRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val recyclerView = mBinding.recyclerView
        (recyclerView.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false

        setupRecyclerView()

        mBinding.searchET.requestFocus()

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
            }
        })

        mBinding.searchBtn.setOnClickListener {
            val searchInput = mBinding.searchET.text.toString()
            setupSearchRecyclerView(searchInput)
        }
    }


    private fun setupRecyclerView() {
        setupSearchRecyclerView("") // Initialize with empty search input to show all users
    }

    private fun setupSearchRecyclerView(searchInput: String) {
        adapter?.stopListening()
        adapter = null

        inProgress(true)

        val formattedInput = searchInput.lowercase().trim()
        val query = if (formattedInput.isEmpty()) {
            // Show all users
            FirebaseUtils.allUserCollectionReference()
                .orderBy("displayNameLowercase")
        } else {
            // Search for specific users using keywords
            FirebaseUtils.allUserCollectionReference()
                .whereArrayContainsAny("searchKeywords", listOf(formattedInput))
        }

        // Fetch the results to check if we have any documents
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshot = task.result
                if (querySnapshot != null && querySnapshot.isEmpty) {
                    noUserFound(true) // Show empty state if no results found
                } else {
                    noUserFound(false) // Hide empty state if there are results
                }
            } else {
                noUserFound(false) // Hide empty state in case of an error
            }
        }

        val options = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = SearchUserRecyclerAdapter(options, this, this)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()

        inProgress(false)
    }

    private fun inProgress(inProgress: Boolean) {
        if (inProgress) {
            mBinding.progressBar.visibility = View.VISIBLE
            mBinding.searchBtn.isEnabled = false
        } else {
            mBinding.progressBar.visibility = View.GONE
            mBinding.searchBtn.isEnabled = true
        }
    }

    private fun noUserFound(noUserFound: Boolean) {
        if (noUserFound) {
            mBinding.emptyLayout.visibility = View.VISIBLE
        } else {
            mBinding.emptyLayout.visibility = View.GONE
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
        adapter?.startListening() // Restart listening in case onResume is called without onStart
    }


    override fun onPause() {
        super.onPause()
        adapter?.stopListening() // Avoid memory leaks or inconsistencies
    }
}