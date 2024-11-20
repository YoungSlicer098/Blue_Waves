package com.dld.bluewaves

import android.os.Bundle
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
            val searchInput: String = mBinding.searchET.text.toString()
            if (searchInput.isEmpty() || searchInput.length < 2) {
                mBinding.searchTil.apply(){
                    isErrorEnabled = true
                    error = "Invalid username"
                    return@setOnClickListener
                }
            }else {
                mBinding.searchTil.apply {
                    isErrorEnabled = false
                    error = null
                }

            }



            setupSearchRecyclerView(searchInput)
        }
    }
    private fun setupSearchRecyclerView(searchInput: String) {
        adapter?.stopListening()
        adapter = null

        val formattedInput = searchInput.lowercase()
        val query = FirebaseUtils.allUserCollectionReference()
            .orderBy("displayNameLowercase")
            .startAt(formattedInput)
            .endAt(formattedInput + "\uf8ff")

        val options = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java)
            .setLifecycleOwner(this) // Automatically starts/stops listening with lifecycle
            .build()

        adapter = SearchUserRecyclerAdapter(options, this, this)
        mBinding.recyclerView.setLayoutManager(LinearLayoutManager(this))
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()
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