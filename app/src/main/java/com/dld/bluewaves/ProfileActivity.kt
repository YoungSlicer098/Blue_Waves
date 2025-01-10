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
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dld.bluewaves.databinding.ActivityBaseDrawerBinding
import com.dld.bluewaves.databinding.ActivityProfileBinding
import com.dld.bluewaves.databinding.DialogContactNumberEditBinding
import com.dld.bluewaves.databinding.DialogDisplayNameEditBinding
import com.dld.bluewaves.databinding.DialogPasswordEditBinding
import com.dld.bluewaves.databinding.DialogPicturesBinding
import com.dld.bluewaves.databinding.DialogProfilePicDecisionsBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.DrawerUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityProfileBinding
    private lateinit var binding: ActivityBaseDrawerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogName: DialogDisplayNameEditBinding
    private lateinit var dialogPassword: DialogPasswordEditBinding
    private lateinit var dialogProfilePic: DialogProfilePicDecisionsBinding
    private lateinit var dialogPictures: DialogPicturesBinding
    private lateinit var dialogContactNumber: DialogContactNumberEditBinding
    private var currentUserModel: UserModel? = null
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private var needsUpdate = false
    private var changedName = false
    private var changedProfilePic = false
    private var changedContactNumber = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDrawerBinding.inflate(layoutInflater)
        mBinding = ActivityProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)
        binding.baseContent.addView(mBinding.root)

        DrawerUtils.setupDrawer(this, binding, binding.toolbar)


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
        mBinding.contactNumberEdit.paint.isUnderlineText = true


        showUpdateDialog()
        updateInProgress(false)

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
                        lifecycleScope.launch {
                            val sameDisplayName = sameDisplayName(dialogName.displayNameET.text.toString())
                            if (sameDisplayName) {
                                currentUserModel?.displayName = dialogName.displayNameET.text.toString()
                                mBinding.displayNameText.text = dialogName.displayNameET.text.toString()
                                needsUpdate = true
                                changedName = true
                                showUpdateDialog()
                                dialog.dismiss()
                            }
                        } // Dismiss the dialog only after a successful update
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
                    val keywords = AndroidUtils.generateSearchKeywords(currentUserModel?.displayName!!) + AndroidUtils.generateSearchKeywords(currentUserModel?.email!!)
                    val updates = mapOf(
                        "displayName" to currentUserModel?.displayName,
                        "displayNameLowercase" to currentUserModel?.displayNameLowercase,
                        "searchKeywords" to keywords.distinct(),
                    )
                    FirebaseUtils.currentUserDetails()
                        .update(updates)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                fadeInAndOut(mBinding.displayNameIconCheck)
                                changedName = false
                            } else {
                                fadeInAndOut(mBinding.displayNameIconClose)
                                changedName = true
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
                                    changedProfilePic = true
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
                                            changedProfilePic = true
                                            needsUpdate = true
                                        }
                                    }
                                }
                            }
                    }
                }

                changedContactNumber -> {
                    FirebaseUtils.currentUserDetails()
                        .update("contactNumber", currentUserModel?.contactNumber).addOnCompleteListener {
                            if (it.isSuccessful) {
                                changedContactNumber = false
                                fadeInAndOut(mBinding.contactNumberIconCheck)
                            } else {
                                changedContactNumber = true
                                needsUpdate = true
                                fadeInAndOut(mBinding.contactNumberIconClose)
                            }
                        }
                }
            }
            if (!changedName && !changedProfilePic && !changedContactNumber) {
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
                initContactNumber(currentUserModel?.contactNumber.toString())

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

    private fun initContactNumber(contactNumber: String) {
        if (contactNumber == "") {
            mBinding.contactNumberText.text = "N/A"
            mBinding.contactNumberEdit.visibility = View.VISIBLE
            mBinding.contactNumberEdit.isEnabled = true
            mBinding.contactNumberEdit.setOnClickListener {
                dialogContactNumber = DialogContactNumberEditBinding.inflate(LayoutInflater.from(this))
                dialogContactNumber.contactNumberET.setText(currentUserModel?.contactNumber)

                dialogContactNumber.contactNumberET.onFocusChangeListener =
                    View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            if (dialogContactNumber.contactNumberTil.isErrorEnabled) {
                                dialogContactNumber.contactNumberTil.isErrorEnabled = false
                            }
                        }else {
                            validateContactNumber(dialogContactNumber)
                        }

                    }

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Edit Contact Number")
                    .setView(dialogContactNumber.root)
                    .setPositiveButton("Save", null) // Initially null to handle manually
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialog.setOnShowListener {
                    val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    saveButton.setOnClickListener {
                        if (validateContactNumber(dialogContactNumber)) {
                            lifecycleScope.launch {
                                val sameContactNumber = sameContactNumber(dialogContactNumber.contactNumberET.text.toString())
                                if (sameContactNumber) {
                                    currentUserModel?.contactNumber = dialogContactNumber.contactNumberET.text.toString()
                                    mBinding.contactNumberText.text = dialogContactNumber.contactNumberET.text.toString()
                                    needsUpdate = true
                                    changedContactNumber = true
                                    showUpdateDialog()
                                    dialog.dismiss()
                                }
                            } // Dismiss the dialog only after a successful update
                        } else {
                            // Keep the dialog open if validation fails
                            AndroidUtils.showToast(this, "Please provide a valid number.")
                        }
                    }
                }

                dialog.show()
            }
        } else {
            mBinding.contactNumberText.text = contactNumber
            mBinding.contactNumberEdit.visibility = View.GONE
            mBinding.contactNumberEdit.isEnabled = false
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
                displayNameLowercase = value.lowercase()
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
        val specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
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
        val specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
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
        val specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
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

    private suspend fun sameDisplayName(displayName: String): Boolean {
        if (FirebaseUtils.sameDisplayNameVerify(displayName)) {
            dialogName.displayNameTil.apply {
                isErrorEnabled = true
                error = "Display name already exists"
                inProgress(false)
            }
        }else {
            dialogName.displayNameTil.isErrorEnabled = false
        }
        return !dialogName.displayNameTil.isErrorEnabled
    }

    private fun validateContactNumber(dialogContactNumber: DialogContactNumberEditBinding): Boolean {
        var errorMessage: String? = null
        val value: String = dialogContactNumber.contactNumberET.text.toString()
        if (!((value.length == 12 && value.substring(0,3) == "639") || (value.length == 11 && value.substring(0, 2) == "09"))) {
            errorMessage = "Contact Number must be 09XXXXXXXXX or 639XXXXXXXXX"
        }

        if (errorMessage != null) {
            dialogContactNumber.contactNumberTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        } else {
            currentUserModel = currentUserModel?.copy(
                contactNumber = value
            )
        }

        return errorMessage == null

    }

    private suspend fun sameContactNumber(contactNumber: String): Boolean {
        if (FirebaseUtils.sameContactNumberVerify(contactNumber)) {
            dialogContactNumber.contactNumberTil.apply {
                isErrorEnabled = true
                error = "Email already exists"
                inProgress(false)
            }
        }else {
            dialogContactNumber.contactNumberTil.isErrorEnabled = false
        }
        return !dialogContactNumber.contactNumberTil.isErrorEnabled
    }

}