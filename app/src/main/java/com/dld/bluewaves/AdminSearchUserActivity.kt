package com.dld.bluewaves

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dld.bluewaves.adapter.AdminSearchUserRecyclerAdapter
import com.dld.bluewaves.adapter.SearchUserRecyclerAdapter
import com.dld.bluewaves.databinding.ActivityAdminBinding
import com.dld.bluewaves.databinding.ActivityAdminSearchUserBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.FirebaseUtils
import com.firebase.ui.firestore.FirestoreRecyclerOptions

@Suppress("DEPRECATION")
class AdminSearchUserActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAdminSearchUserBinding
    private var adapter: AdminSearchUserRecyclerAdapter? = null

    public override fun onStart() {
        super.onStart()
        FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result
                val role = user?.getString("role")
                when (role) {
                    "admin", "developer" -> {
                        return@addOnCompleteListener
                    }
                    else -> {
                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        adapter?.startListening() // Ensure adapter starts listening here
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAdminSearchUserBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupRecyclerView()

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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

        adapter = AdminSearchUserRecyclerAdapter(options, this, this)
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