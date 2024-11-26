package com.dld.bluewaves

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dld.bluewaves.databinding.ActivityProfileBinding
import com.dld.bluewaves.databinding.DialogDisplayNameEditBinding
import com.dld.bluewaves.databinding.DialogPasswordEditBinding
import com.dld.bluewaves.databinding.DialogPicturesBinding
import com.dld.bluewaves.databinding.DialogProfilePicDecisionsBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storageMetadata

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogName: DialogDisplayNameEditBinding
    private lateinit var dialogPassword: DialogPasswordEditBinding
    private lateinit var dialogProfilePic: DialogProfilePicDecisionsBinding
    private lateinit var dialogPictures: DialogPicturesBinding
    private var currentUserModel: UserModel? = null
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private var needsUpdate = false
    private var changedName = false
    private var changedProfilePic = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(mBinding.root)

        val user = auth.currentUser
        if (user == null) {
            AndroidUtils.showToast(this, "No permission!")
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        initTransitions()

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
        showUpdateDialog()
        updateInProgress(false)

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

        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) {
                        selectedImageUri = data.data!!
                        AndroidUtils.setProfilePic(
                            context = this,
                            selectedImageUri,
                            mBinding.profilePic
                        )
                        needsUpdate = true
                        changedProfilePic = true
                        currentUserModel?.profilePic = ""
                        showUpdateDialog()
                    }
                }
            }

        //On Click Listeners Main Functions
        mBinding.profilePic.setOnClickListener {
            dialogProfilePic = DialogProfilePicDecisionsBinding.inflate(LayoutInflater.from(this))

            // Create the AlertDialog
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Profile Picture")
                .setView(dialogProfilePic.root)
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            // Set click listeners to close the dialog after performing actions
            val closeDialogAndPerformAction: (() -> Unit) -> View.OnClickListener = { action ->
                View.OnClickListener {
                    action() // Perform your action (e.g., uploadProfilePicture())
                    alertDialog.dismiss() // Close the dialog
                }
            }

            dialogProfilePic.uploadBtn.setOnClickListener(closeDialogAndPerformAction(::uploadProfilePicture))
            dialogProfilePic.uploadText.setOnClickListener(closeDialogAndPerformAction(::uploadProfilePicture))
            dialogProfilePic.uploadLayout.setOnClickListener(closeDialogAndPerformAction(::uploadProfilePicture))

            dialogProfilePic.picturesBtn.setOnClickListener(closeDialogAndPerformAction(::picturesProfilePicture))
            dialogProfilePic.picturesText.setOnClickListener(closeDialogAndPerformAction(::picturesProfilePicture))
            dialogProfilePic.picturesLayout.setOnClickListener(closeDialogAndPerformAction(::picturesProfilePicture))

            alertDialog.show()
        }

        mBinding.displayNameEdit.setOnClickListener {
            dialogName = DialogDisplayNameEditBinding.inflate(LayoutInflater.from(this))
            dialogName.displayNameET.setText(currentUserModel?.displayName)

            dialogName.displayNameET.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
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
                        mBinding.displayNameText.text = dialogName.displayNameET.text.toString()
                        needsUpdate = true
                        changedName = true
                        showUpdateDialog()
                        dialog.dismiss() // Dismiss the dialog only after a successful update
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

            dialogPassword.oPasswordET.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        if (dialogPassword.oPasswordTil.isErrorEnabled) {
                            dialogPassword.oPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        validateOPassword(dialogPassword)
                    }
                }
            dialogPassword.nPasswordET.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        if (dialogPassword.nPasswordTil.isErrorEnabled) {
                            dialogPassword.nPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        validateNPassword(dialogPassword)
                        if (validateNPassword(dialogPassword) && dialogPassword.cPasswordET.text!!.isNotEmpty() && validateConfirmPassword(
                                dialogPassword
                            ) && validateNPasswordAndConfirmPassword(dialogPassword)
                        ) {
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
            dialogPassword.cPasswordET.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        if (dialogPassword.cPasswordTil.isErrorEnabled) {
                            dialogPassword.cPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        validateConfirmPassword(dialogPassword)
                        if (validateConfirmPassword(dialogPassword) && validateNPassword(
                                dialogPassword
                            ) && validateNPasswordAndConfirmPassword(dialogPassword)
                        ) {
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
                    fadeInAndOut(mBinding.passwordIconClose)
                    dialogInterface.dismiss()
                }
                .create()

            dialog.setOnShowListener {
                val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {

                    if (validateOPassword(dialogPassword) &&
                        validateNPassword(dialogPassword) &&
                        validateConfirmPassword(dialogPassword) &&
                        validateNPasswordAndConfirmPassword(dialogPassword)
                    ) {

                        val email = user?.email
                        val password = dialogPassword.oPasswordET.text.toString()
                        val nPassword = dialogPassword.nPasswordET.text.toString()
                        val credential: AuthCredential =
                            EmailAuthProvider.getCredential(email!!, password)

                        user.reauthenticate(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                user.updatePassword(nPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        AndroidUtils.showToast(
                                            this,
                                            "Password Successfully Updated!"
                                        )
                                        fadeInAndOut(mBinding.passwordIconCheck)
                                        dialog.dismiss()
                                    } else {
                                        AndroidUtils.showToast(
                                            this,
                                            "Something went wrong. Please try again."
                                        )
                                        fadeInAndOut(mBinding.passwordIconClose)
                                    }
                                }
                            } else {
                                validateOPasswordWrong(dialogPassword)
                                fadeInAndOut(mBinding.passwordIconClose)
                            }
                        }
                    } else {
                        AndroidUtils.showToast(this, "Please correct the errors before saving.")
                        fadeInAndOut(mBinding.passwordIconClose)
                    }
                }
            }

            dialog.show()
        }

        mBinding.saveBtn.setOnClickListener {
            updateUserData()
            showUpdateDialog()
        }

        mBinding.undoBtn.setOnClickListener {
            getUserData()
            needsUpdate = false
            changedName = false
            changedProfilePic = false
            showUpdateDialog()
        }


        getUserData()
    }

    private fun picturesProfilePicture() {
        dialogPictures = DialogPicturesBinding.inflate(LayoutInflater.from(this))

        var selectedProfilePic: String = ""

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Choose a Picture")
            .setView(dialogPictures.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.setOnShowListener {
            val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                if (selectedProfilePic != "") {
                    currentUserModel?.profilePic = selectedProfilePic
                    mBinding.profilePic.setImageResource(AndroidUtils.selectPicture(selectedProfilePic))
                    needsUpdate = true
                    changedProfilePic = true
                    showUpdateDialog()
                    alertDialog.dismiss()
                } else {
                    AndroidUtils.showToast(this, "No picture selected")
                }
            }
        }

        // ImageButton IDs
        val imageViews = listOf(
            dialogPictures.pfp1,
            dialogPictures.pfp2,
            dialogPictures.pfp3,
            dialogPictures.pfp4,
            dialogPictures.pfp5,
            dialogPictures.pfp6
        )

        val drawableIds = listOf(
            R.drawable.pfp1,
            R.drawable.pfp2,
            R.drawable.pfp3,
            R.drawable.pfp4,
            R.drawable.pfp5,
            R.drawable.pfp6
        )

        // Listener to handle selection
        val onImageSelected = { selectedView: ImageView, drawableId: Int ->
            // Reset the selection for all ImageButtons
            imageViews.forEach { it.isSelected = false }

            // Set the selected ImageButton
            selectedView.isSelected = true

            // Update the selected profile picture
            selectedProfilePic = when (drawableId){
                R.drawable.pfp1 -> "pfp1"
                R.drawable.pfp2 -> "pfp2"
                R.drawable.pfp3 -> "pfp3"
                R.drawable.pfp4 -> "pfp4"
                R.drawable.pfp5 -> "pfp5"
                R.drawable.pfp6 -> "pfp6"
                else -> ""
            }
        }



        // Set click listeners for each ImageButton
        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                onImageSelected(imageView, drawableIds[index])
            }
        }

        alertDialog.show()
    }

    private fun uploadProfilePicture() {
        ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
            .createIntent { intent ->
                imagePickLauncher.launch(intent)
            }
    }

    private fun showUpdateDialog() {
        if (needsUpdate) {
            mBinding.updateLayout.visibility = View.VISIBLE
        } else {
            mBinding.updateLayout.visibility = View.GONE
        }
    }

    private fun updateUserData() {
        if (needsUpdate) {
            updateInProgress(true)
            needsUpdate = false

            when {
                changedName -> {
                    FirebaseUtils.currentUserDetails()
                        .update("displayName", currentUserModel?.displayName)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                fadeInAndOut(mBinding.displayNameIconCheck)
                                changedName = false
                            } else {
                                fadeInAndOut(mBinding.displayNameIconClose)
                                needsUpdate = true
                            }
                        }
                }

                changedProfilePic -> {
                    if (currentUserModel?.profilePic != ""){
                        FirebaseUtils.currentUserDetails().update("profilePic", currentUserModel?.profilePic)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    FirebaseUtils.getCurrentProfilePicStorageRef().delete()
                                    fadeInAndOut(mBinding.profilePicIconCheck)
                                    changedProfilePic = false
                                } else{
                                    fadeInAndOut(mBinding.profilePicIconClose)
                                    needsUpdate = true
                                }
                            }
                    }else {
                        val metadata = storageMetadata {
                            cacheControl = "public, max-age=31536000" // Cache for 1 year
                        }
                        FirebaseUtils.getCurrentProfilePicStorageRef()
                            .putFile(selectedImageUri, metadata).addOnCompleteListener { img ->
                                if (img.isSuccessful) {
                                    FirebaseUtils.currentUserDetails().update("profilePic", "").addOnCompleteListener { it ->
                                        if (it.isSuccessful){
                                            fadeInAndOut(mBinding.profilePicIconCheck)
                                            changedProfilePic = false
                                        }else{
                                            fadeInAndOut(mBinding.profilePicIconClose)
                                            needsUpdate = true
                                        }
                                    }
                                }
                            }
                    }
                }
            }
            if (!changedName && !changedProfilePic) {
                AndroidUtils.showToast(this, "Changes saved")
                needsUpdate = false
            }
            updateInProgress(false)
        }
    }

    private fun updateInProgress(progress: Boolean) {
        if (progress) {
            mBinding.updateProgressBar.visibility = View.VISIBLE
            mBinding.saveBtn.visibility = View.GONE
        } else {
            mBinding.updateProgressBar.visibility = View.GONE
            mBinding.saveBtn.visibility = View.VISIBLE
        }
    }

    private fun initTransitions() {
        val transitionUpdate: Transition = Slide(Gravity.BOTTOM)
        transitionUpdate.duration = 300
        transitionUpdate.addTarget(mBinding.updateLayout)

        TransitionManager.beginDelayedTransition(mBinding.root, transitionUpdate)

    }

    private fun fadeInAndOut(view: View, duration: Long = 3000L, fadeDuration: Long = 1000L) {
        // Set the view to be fully visible at the start
        view.alpha = 1f
        view.visibility = View.VISIBLE

        // Create a handler to start fading after 2 seconds (2000ms)
        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            fadeOut.duration = fadeDuration
            fadeOut.interpolator = DecelerateInterpolator()
            fadeOut.start()

            // Optionally hide the view after the fade-out completes
            fadeOut.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }, duration) // 2-second delay before fading
    }

    private fun getUserData() {
        inProgress(true)

        FirebaseUtils.currentUserDetails().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                inProgress(false)
                currentUserModel = task.result.toObject(UserModel::class.java)
                mBinding.displayNameText.text = currentUserModel?.displayName
                mBinding.emailText.text = currentUserModel?.email
                mBinding.roleText.text = currentUserModel?.role?.uppercase()

                if (currentUserModel?.profilePic != "") {
                    mBinding.profilePic.setImageResource(AndroidUtils.selectPicture(currentUserModel?.profilePic!!))
                }else{
                    FirebaseUtils.getCurrentProfilePicStorageRef().downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val uri: Uri = it.result
                            AndroidUtils.setProfilePic(context = this, uri, mBinding.profilePic)
                        } else {
                            // Optionally, set a placeholder or fallback image for failed loads
                            mBinding.profilePic.setImageResource(R.drawable.profile_white)
                        }
                    }
                }


            } else {
                currentUserModel = UserModel()
                FirebaseUtils.getCurrentProfilePicStorageRef().downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val uri: Uri = it.result
                        AndroidUtils.setProfilePic(context = this, uri, mBinding.profilePic)
                    } else {
                        // Optionally, set a placeholder or fallback image for failed loads
                        mBinding.profilePic.setImageResource(R.drawable.profile_white)
                    }
                }
                AndroidUtils.showToast(this, "Could not gather data")
            }
        }
    }

    private fun inProgress(progress: Boolean) {
        if (progress) {
            mBinding.progressBar.visibility = View.VISIBLE
            mBinding.passwordEdit.isEnabled = false
            mBinding.displayNameEdit.isEnabled = false
            mBinding.profilePic.isEnabled = false
        } else {
            mBinding.progressBar.visibility = View.GONE
            mBinding.passwordEdit.isEnabled = true
            mBinding.displayNameEdit.isEnabled = true
            mBinding.profilePic.isEnabled = true
        }
    }


    private fun generateSearchKeywords(displayName: String): List<String> {
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
        } else if (value == mBinding.displayNameText.text) {
            errorMessage = "Display name must be different from current name"
        }

        if (errorMessage != null) {
            dialogName.displayNameTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        } else {
            currentUserModel = currentUserModel?.copy(
                displayName = value,
                displayNameLowercase = value.lowercase(),
                searchKeywords = generateSearchKeywords(value)
            )
        }


        return errorMessage == null
    }

    private fun validateOPasswordWrong(dialogPassword: DialogPasswordEditBinding) {
        dialogPassword.oPasswordTil.apply {
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
            errorMessage =
                "Old Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
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
            errorMessage =
                "New Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
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
            errorMessage =
                "Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
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
            R.id.nav_customization -> Toast.makeText(this, "Customization!", Toast.LENGTH_SHORT)
                .show()

            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }

            R.id.nav_logout -> {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                    if (it.isSuccessful) {
                        FirebaseAuth.getInstance().signOut()
                        AndroidUtils.showToast(this, "Logged out!")
                        validationSideBar(FirebaseAuth.getInstance())
                        val intent = Intent(this, SplashActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                }
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