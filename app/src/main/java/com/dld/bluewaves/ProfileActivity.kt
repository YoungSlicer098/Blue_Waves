package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dld.bluewaves.databinding.ActivityProfileBinding
import com.dld.bluewaves.databinding.DialogDisplayNameEditBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogName: DialogDisplayNameEditBinding
    private var currentUserModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(mBinding.root)

        val user = auth.currentUser
        if(user == null){
            AndroidUtils.showToast(this, "No permission!")
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        mBinding.displayNameEdit.paint.isUnderlineText = true
        mBinding.passwordEdit.paint.isUnderlineText = true


        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Enable the default ActionBarDrawerToggle for the hamburger menu
        toggle = ActionBarDrawerToggle(
            this, mBinding.drawerLayout, mBinding.toolbar, R.string.open_nav, R.string.close_nav
        )
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        validationSideBar(auth)

        mBinding.toolbar.setNavigationOnClickListener {
            if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                mBinding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                mBinding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Initialize the navigation view and set the listener
        mBinding.sidebarNav.setNavigationItemSelectedListener(this)

        // Close button in the sidebar
        val headerLayout = mBinding.sidebarNav.getHeaderView(0)
        val navCloseBtn = headerLayout.findViewById<ImageView>(R.id.navCloseBtn)
        navCloseBtn.setOnClickListener {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
            }
        })

        mBinding.displayNameEdit.setOnClickListener {
            AndroidUtils.showToast(this, "Hello")
            dialogName = DialogDisplayNameEditBinding.inflate(LayoutInflater.from(this))
            dialogName.displayNameET.setText(currentUserModel?.displayName)
            AlertDialog.Builder(this)
                .setTitle("Edit Display Name")
                .setView(dialogName.root)
                .setPositiveButton("Save") { dialogInterface, _ ->
                    if(validateDisplayName(dialogName)){
                        FirebaseUtils.currentUserDetails().set(currentUserModel!!).addOnCompleteListener {
                            if (it.isSuccessful){
                                AndroidUtils.showToast(this, "Name updated")
                                mBinding.displayNameText.text = dialogName.displayNameET.text.toString()
                                dialogInterface.dismiss()
                            }else{
                                AndroidUtils.showToast(this, "Could not update name")
                            }

                        }
                    }
                }
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
                .show()
        }
        mBinding.passwordEdit.setOnClickListener {
            AndroidUtils.showToast(this, "Hello This is Password")
        }

        getUserData()
    }

    private fun getUserData(){
        FirebaseUtils.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUserModel = task.result.toObject(UserModel::class.java)
                mBinding.displayNameText.text = currentUserModel?.displayName
                mBinding.emailText.text = currentUserModel?.email
                mBinding.roleText.text = currentUserModel?.role

            }else{
                currentUserModel = UserModel()
                AndroidUtils.showToast(this, "Could not gather data")
            }
        }
    }


    private fun updateFirestoreName(){
        FirebaseUtils.currentUserDetails().set(currentUserModel!!).addOnCompleteListener {
            if (it.isSuccessful){
                AndroidUtils.showToast(this, "Name updated")
            }else{
                AndroidUtils.showToast(this, "Could not update name")
            }

        }
    }


    private fun validateDisplayName(dialogName: DialogDisplayNameEditBinding): Boolean {
        var errorMessage: String? = null
        val value: String = dialogName.displayNameET.text.toString()
        val specialCharactersRegex = Regex("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
        if (value.isEmpty()) {
            errorMessage = "Display name is required"
        } else if (specialCharactersRegex.containsMatchIn(value)) {
            errorMessage = "Display name must not contain special characters"
        } else if (value.length < 2) {
            errorMessage = "Display name must be at least 2 characters"
        }

        if (errorMessage != null) {
            dialogName.displayNameTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        } else{
            currentUserModel = currentUserModel?.copy(
                displayName = value,
                displayNameLowercase = value.lowercase()
            )
        }


        return errorMessage == null
    }

    override fun onResume() {
        super.onResume()
        validationSideBar(FirebaseAuth.getInstance())
    }

    private fun validationSideBar(auth: FirebaseAuth) {
        val user = auth.currentUser

        if (user == null) {
            // If user is not logged in, disable certain items
            mBinding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = false
        } else {
            // If user is logged in, enable all items
            mBinding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = true
        }
        mBinding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = false

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_customization -> Toast.makeText(this, "Customization!", Toast.LENGTH_SHORT).show()
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                AndroidUtils.showToast(this, "Logged out!")
                validationSideBar(FirebaseAuth.getInstance())
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }
        validationSideBar(FirebaseAuth.getInstance())
        mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}