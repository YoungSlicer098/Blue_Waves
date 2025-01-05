package com.dld.bluewaves

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dld.bluewaves.adapter.SearchUserRecyclerAdapter
import com.dld.bluewaves.databinding.ActivitySearchUserBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions

@Suppress("DEPRECATION")
class SearchUserActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySearchUserBinding
    private var adapter: SearchUserRecyclerAdapter? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val recyclerView = mBinding.recyclerView
        (recyclerView.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations =
            false

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

        mBinding.searchBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = 0.95f
                    v.scaleY = 0.95f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }
            false // Return false to allow the click event to proceed
        }

        // Listen for the Enter key on the keyboard
        mBinding.searchET.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val searchInput = mBinding.searchET.text.toString()
                setupSearchRecyclerView(searchInput)
                true // Indicate that the action has been handled
            } else {
                false // Let the system handle other actions
            }
        }
    }


    private fun setupRecyclerView() {
        setupSearchRecyclerView("") // Initialize with empty search input to show all users
    }

    private fun setupSearchRecyclerView(searchInput: String) {
        adapter?.stopListening()
        adapter = null

        inProgress(true)

        // Get the current user's role
        FirebaseUtils.currentUserDetails().get().addOnCompleteListener { currentUserTask ->
            if (currentUserTask.isSuccessful) {
                val currentUserRole = currentUserTask.result?.getString("role")?.lowercase()

                val formattedInput = searchInput.lowercase().trim()
                val query = when {
                    // Limit "user" role to search for specific roles
                    currentUserRole in listOf("user", "customer", "paid customer", "unpaid customer") -> {
                        if (formattedInput.isEmpty()) {
                            FirebaseUtils.allUserCollectionReference()
                                .whereIn("role", listOf("staff", "admin", "developer"))
                                .orderBy("displayNameLowercase")
                        } else {
                            FirebaseUtils.allUserCollectionReference()
                                .whereIn("role", listOf("staff", "admin", "developer"))
                                .whereArrayContainsAny("searchKeywords", listOf(formattedInput))
                        }
                    }

                    // "staff," "admin," and "developer" can search for all users
                    currentUserRole in listOf("staff", "admin", "developer") -> {
                        if (formattedInput.isEmpty()) {
                            FirebaseUtils.allUserCollectionReference()
                                .orderBy("displayNameLowercase")
                        } else {
                            FirebaseUtils.allUserCollectionReference()
                                .whereArrayContainsAny("searchKeywords", listOf(formattedInput))
                        }
                    }

                    // Default to showing no results if role is null or unknown
                    else ->
                        if (formattedInput.isEmpty()) {
                            FirebaseUtils.allUserCollectionReference()
                                .whereIn("role", listOf("staff", "admin", "developer"))
                                .orderBy("displayNameLowercase")
                        } else {
                            FirebaseUtils.allUserCollectionReference()
                                .whereIn("role", listOf("staff", "admin", "developer"))
                                .whereArrayContainsAny("searchKeywords", listOf(formattedInput))
                        }
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
            } else {
                // Handle errors while fetching current user's role
                inProgress(false)
                noUserFound(true)
            }
        }
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