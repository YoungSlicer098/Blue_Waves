package com.dld.bluewaves

import android.os.Bundle
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

        mBinding.searchET.requestFocus()

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
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

        val formattedInput = searchInput.lowercase()

        val query: Query = FirebaseUtils.allUserCollectionReference()
            .orderBy("displayNameLowercase")
            .startAt(formattedInput)
            .endAt(formattedInput + "\uf8ff")

        val options: FirestoreRecyclerOptions<UserModel> = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java).build()

        adapter = SearchUserRecyclerAdapter(options, applicationContext)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerView.adapter = adapter
        adapter?.startListening()
    }

    override fun onStart() {
        super.onStart()
        if(adapter!=null){
            adapter!!.startListening()
        }
    }
    override fun onStop() {
        super.onStop()
        if(adapter!=null){
            adapter!!.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if(adapter!=null){
            adapter!!.startListening()
        }
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
    }
}