package com.dld.bluewaves

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
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
import com.dld.bluewaves.databinding.DialogPasswordEditBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogName: DialogDisplayNameEditBinding
    private lateinit var dialogPassword: DialogPasswordEditBinding
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
            dialogName = DialogDisplayNameEditBinding.inflate(LayoutInflater.from(this))
            dialogName.displayNameET.setText(currentUserModel?.displayName)

            dialogName.displayNameET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (dialogName.displayNameTil.isErrorEnabled) {
                        dialogName.displayNameTil.isErrorEnabled = false
                    }
                } else {
                    validateDisplayName(dialogName)

                }
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Edit Display Name")
                .setView(dialogName.root)
                .setPositiveButton("Save", null) // Initially null to handle manually
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Custom behavior for the Save button
            dialog.setOnShowListener {
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    if (validateDisplayName(dialogName)) {
                        currentUserModel?.displayName = dialogName.displayNameET.text.toString()
                        FirebaseUtils.currentUserDetails().set(currentUserModel!!).addOnCompleteListener {
                            if (it.isSuccessful) {
                                AndroidUtils.showToast(this, "Name updated")
                                mBinding.displayNameText.text = dialogName.displayNameET.text.toString()
                                dialog.dismiss() // Dismiss the dialog only after a successful update
                            } else {
                                AndroidUtils.showToast(this, "Could not update name")
                            }
                        }
                    } else {
                        // Keep the dialog open if validation fails
                        AndroidUtils.showToast(this, "Please provide a valid name.")
                    }
                }
            }

            dialog.show()
        }


        mBinding.passwordEdit.setOnClickListener {
            dialogPassword = DialogPasswordEditBinding.inflate(LayoutInflater.from(this))

            dialogPassword.oPasswordET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (dialogPassword.oPasswordTil.isErrorEnabled) {
                        dialogPassword.oPasswordTil.isErrorEnabled = false
                    }
                }else{
                    validateOPassword(dialogPassword)
                }
            }
            dialogPassword.nPasswordET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (dialogPassword.nPasswordTil.isErrorEnabled) {
                        dialogPassword.nPasswordTil.isErrorEnabled = false
                    }
                }else{
                    validateNPassword(dialogPassword)
                    if (validateNPassword(dialogPassword) && dialogPassword.cPasswordET.text!!.isNotEmpty() && validateConfirmPassword(dialogPassword) && validateNPasswordAndConfirmPassword(dialogPassword)) {
                        if (dialogPassword.cPasswordTil.isErrorEnabled) {
                            dialogPassword.cPasswordTil.isErrorEnabled = false
                        }
                        dialogPassword.cPasswordTil.apply {
                            setStartIconDrawable(R.drawable.blue_check_circle)
                            setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                        }
                    }
                }
            }
            dialogPassword.cPasswordET.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (dialogPassword.cPasswordTil.isErrorEnabled) {
                        dialogPassword.cPasswordTil.isErrorEnabled = false
                    }
                }else{
                    validateConfirmPassword(dialogPassword)
                    if (validateConfirmPassword(dialogPassword) && validateNPassword(dialogPassword) && validateNPasswordAndConfirmPassword(dialogPassword)) {
                        if (dialogPassword.nPasswordTil.isErrorEnabled) {
                            dialogPassword.nPasswordTil.isErrorEnabled = false
                        }
                        dialogPassword.nPasswordTil.apply {
                            setStartIconDrawable(R.drawable.blue_check_circle)
                            setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                        }
                    }
                }
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogPassword.root)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            dialog.setOnShowListener {
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {

                    if (validateOPassword(dialogPassword) &&
                        validateNPassword(dialogPassword) &&
                        validateConfirmPassword(dialogPassword) &&
                        validateNPasswordAndConfirmPassword(dialogPassword)) {

                        val email = user?.email
                        val password = dialogPassword.oPasswordET.text.toString()
                        val nPassword = dialogPassword.nPasswordET.text.toString()
                        val credential: AuthCredential = EmailAuthProvider.getCredential(email!!, password)

                        user.reauthenticate(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                user.updatePassword(nPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        AndroidUtils.showToast(this, "Password Successfully Updated!")
                                        dialog.dismiss()
                                    } else {
                                        AndroidUtils.showToast(this, "Something went wrong. Please try again.")
                                    }
                                }
                            } else {
                                validateOPasswordWrong(dialogPassword)
                            }
                        }
                    } else {
                        AndroidUtils.showToast(this, "Please correct the errors before saving.")
                    }
                }
            }

            dialog.show()
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


    fun generateSearchKeywords(displayName: String): List<String> {
        val keywords = mutableSetOf<String>()
        val words = displayName.lowercase().split(" ")

        // Add full name and individual words
        keywords.add(displayName.lowercase())
        keywords.addAll(words)

        // Add substrings for each word
        words.forEach { word ->
            for (i in 1..word.length) {
                for (j in 0..word.length - i) {
                    keywords.add(word.substring(j, j + i))
                }
            }
        }

        return keywords.toList()
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
                displayNameLowercase = value.lowercase(),
                searchKeywords = generateSearchKeywords(value)
            )
        }


        return errorMessage == null
    }

    private fun validateOPasswordWrong(dialogPassword: DialogPasswordEditBinding) {
        dialogPassword.oPasswordTil.apply{
            isErrorEnabled = true
            error = "Wrong password"
        }
    }

    private fun validateOPassword(dialogPassword: DialogPasswordEditBinding): Boolean {
        var errorMessage: String? = null
        var specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
        val value: String = dialogPassword.oPasswordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Old Password is required"
        } else if (value.length < 8) {
            errorMessage = "Old Password must be at least 8 characters"
        } else if (!Regex(".*[$specialCharacters].*").containsMatchIn(value)) {
            errorMessage = "Old Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
        }

        if (errorMessage != null) {
            dialogPassword.oPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateNPassword(dialogPassword: DialogPasswordEditBinding): Boolean {
        var errorMessage: String? = null
        var specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
        val value: String = dialogPassword.nPasswordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "New Password is required"
        } else if (value.length < 8) {
            errorMessage = "New Password must be at least 8 characters"
        } else if (!Regex(".*[$specialCharacters].*").containsMatchIn(value)) {
            errorMessage = "New Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
        }

        if (errorMessage != null) {
            dialogPassword.nPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(dialogPassword: DialogPasswordEditBinding): Boolean {
        var errorMessage: String? = null
        var specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
        val value: String = dialogPassword.cPasswordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Confirm password is required"
        } else if (value.length < 8) {
            errorMessage = "Confirm password must be at least 8 characters"
        } else if (!Regex(".*[$specialCharacters].*").containsMatchIn(value)) {
            errorMessage = "Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
        }

        if (errorMessage != null) {
            dialogPassword.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateNPasswordAndConfirmPassword(dialogPassword: DialogPasswordEditBinding): Boolean {
        var errorMessage: String? = null
        val password = dialogPassword.nPasswordET.text.toString()
        val confirmPassword = dialogPassword.cPasswordET.text.toString()
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        }

        if (errorMessage != null) {
            dialogPassword.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
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