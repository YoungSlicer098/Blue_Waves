package com.dld.bluewaves

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dld.bluewaves.databinding.ActivityAdminUserEditBinding
import com.dld.bluewaves.databinding.DialogContactNumberEditBinding
import com.dld.bluewaves.databinding.DialogDisplayNameEditBinding
import com.dld.bluewaves.databinding.DialogPicturesBinding
import com.dld.bluewaves.databinding.DialogProfilePicDecisionsBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class AdminUserEditActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAdminUserEditBinding
    private lateinit var dialogName: DialogDisplayNameEditBinding
    private lateinit var dialogProfilePic: DialogProfilePicDecisionsBinding
    private lateinit var dialogPictures: DialogPicturesBinding
    private lateinit var dialogContactNumber: DialogContactNumberEditBinding
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var model: UserModel
    private var needsUpdate = false
    private var changedName = false
    private var changedRole = false
    private var changedContactNumber = false
    private var changedProfilePic = false

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
                        AndroidUtils.showToast(this, "You are not authorized to access this page!")
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAdminUserEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        model = AndroidUtils.getAllUserModelFromIntent(intent)

        initTransitions()

        mBinding.displayNameEdit.paint.isUnderlineText = true
        mBinding.passwordEdit.paint.isUnderlineText = true


        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
                        model.profilePic = ""
                        showUpdateDialog()
                    }
                }
            }


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
            dialogName.displayNameET.setText(model.displayName)

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
                                model.displayName = dialogName.displayNameET.text.toString()
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
            showPasswordResetDialog(model.email)
        }

        mBinding.contactNumberEdit.setOnClickListener {
            dialogContactNumber = DialogContactNumberEditBinding.inflate(LayoutInflater.from(this))
            dialogContactNumber.contactNumberET.setText(model.contactNumber)

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
                                model.contactNumber = dialogContactNumber.contactNumberET.text.toString()
                                mBinding.contactNumber.text = dialogContactNumber.contactNumberET.text.toString()
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

        mBinding.banBtn.setOnClickListener {
            showBanUserDialog()
        }

        mBinding.unbanBtn.setOnClickListener {
            showUnBanUserDialog()
        }

//        mBinding.deleteAccountBtn.setOnClickListener{
//            showDeleteUserDialog()
//        }


        mBinding.saveBtn.setOnClickListener {
            updateUserData()
            showUpdateDialog()
        }

        mBinding.undoBtn.setOnClickListener {
            getUserData()
            showUpdateDialog()
        }

        getUserData()
        showUpdateDialog()
        readBan(model.banned)
    }



    private fun readBan(banned: Boolean){
        if (banned) {
            mBinding.banBtn.visibility = View.GONE
            mBinding.unbanBtn.visibility = View.VISIBLE
        } else {
            mBinding.banBtn.visibility = View.VISIBLE
            mBinding.unbanBtn.visibility = View.GONE
        }
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
                    model.profilePic = selectedProfilePic
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
                    val keywords = AndroidUtils.generateSearchKeywords(model.displayName) + AndroidUtils.generateSearchKeywords(model.email)
                    val updates = mapOf(
                        "displayName" to model.displayName,
                        "displayNameLowercase" to model.displayNameLowercase,
                        "searchKeywords" to keywords.distinct(),
                    )
                    FirebaseUtils.allUserCollectionReference().document(model.userId)
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
                    if (model.profilePic != ""){
                        FirebaseUtils.allUserCollectionReference().document(model.userId).update("profilePic", model.profilePic)
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
                        FirebaseUtils.getOtherProfilePicStorageRef(model.userId)
                            .putFile(selectedImageUri, metadata).addOnCompleteListener { img ->
                                if (img.isSuccessful) {
                                    FirebaseUtils.allUserCollectionReference().document(model.userId).update("profilePic", "").addOnCompleteListener { it ->
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
                changedRole -> {
                    FirebaseUtils.allUserCollectionReference().document(model.userId)
                        .update("role", model.role).addOnCompleteListener {
                        if (it.isSuccessful) {
                            changedRole = false
                            fadeInAndOut(mBinding.roleIconCheck)
                        } else {
                            changedRole = true
                            needsUpdate = true
                            fadeInAndOut(mBinding.roleIconClose)
                        }
                    }

                }

                changedContactNumber -> {
                    FirebaseUtils.allUserCollectionReference().document(model.userId)
                        .update("contactNumber", model.contactNumber).addOnCompleteListener {
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
            if (!changedName && !changedProfilePic && !changedRole && !changedContactNumber) {
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

        FirebaseUtils.allUserCollectionReference().document(model.userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                inProgress(false)
                model = task.result.toObject(UserModel::class.java)!!
                mBinding.userIDText.text = model.userId
                mBinding.displayNameText.text = model.displayName
                mBinding.emailText.text = model.email
                mBinding.contactNumberText.text = model.contactNumber
                mBinding.lastSessionText.text = model.lastSession?.toDate()?.time?.let {
                    TimeUtils.formatDateToFullMonthDayYearWithTime(it)
                } ?: "No session recorded"

                if (model.role.lowercase() == "developer") {
                    mBinding.roleText.visibility = View.VISIBLE
                    mBinding.roleSpinner.visibility = View.GONE
                    mBinding.roleText.text = model.role.uppercase()
                } else if (model.role.lowercase() in listOf("customer", "paid customer", "unpaid customer", "staff", "admin")) {
                    val roles = listOf("Customer", "Paid Customer", "Unpaid Customer", "Staff", "Admin")
                    val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    mBinding.roleSpinner.adapter = spinnerAdapter

                    // Set the spinner selection based on model.role
                    val currentRole = model.role.lowercase() // Replace with your actual model variable
                    val selectedIndex = roles.indexOf(currentRole)
                    if (selectedIndex != -1) {
                        mBinding.roleSpinner.setSelection(selectedIndex)
                    }

                    // Detect spinner selection changes
                    mBinding.roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val selectedRole = roles[position].lowercase()
                            if (selectedRole != currentRole) {
                                needsUpdate = true
                                changedRole = true
                                model.role = selectedRole
                                showUpdateDialog()
                            } else if (changedProfilePic || changedName || changedContactNumber) {
                                needsUpdate = true
                                changedRole = false
                                showUpdateDialog()
                            } else {
                                needsUpdate = false
                                changedRole = false
                                showUpdateDialog()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // No action needed
                        }
                    }
                } else {
                    mBinding.roleText.visibility = View.VISIBLE
                    mBinding.roleSpinner.visibility = View.GONE
                    mBinding.roleText.text = model.role.uppercase()
                    mBinding.roleText.setTextColor(ContextCompat.getColor(this, R.color.red))
                    mBinding.roleText.paint.isUnderlineText = true
                    mBinding.roleText.setOnClickListener {
                        showRoleResetDialog()
                    }
                }

                if (model.profilePic != "") {
                    mBinding.profilePic.setImageResource(AndroidUtils.selectPicture(model.profilePic))
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

                if (model.banned) {
                    mBinding.banBtn.visibility = View.GONE
                    mBinding.unbanBtn.visibility = View.VISIBLE
                } else {
                    mBinding.banBtn.visibility = View.VISIBLE
                    mBinding.unbanBtn.visibility = View.GONE
                }


            } else {
                model = UserModel()
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

//    private fun showDeleteUserDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Delete User?")
//            .setMessage("Are you sure that you want to delete this user?")
//            .setPositiveButton("Yes") { dialog, _ ->
//                val aBuilder = AlertDialog.Builder(this)
//                    .setTitle("Are you really sure?")
//                    .setPositiveButton("YES") { aDialog, _ ->
//                        inProgress(true)
//                        // First, delete the user document from Firestore
//                        FirebaseUtils.allUserCollectionReference().document(model.userId).delete().addOnCompleteListener { firestoreTask ->
//                            if (firestoreTask.isSuccessful) {
//                                // If the document deletion succeeds, delete the user from FirebaseAuth
//                                val currentUser = FirebaseAuth.getInstance().currentUser
//                                if (currentUser != null && currentUser.uid == model.userId) {
//                                    currentUser.delete().addOnCompleteListener { authTask ->
//                                        if (authTask.isSuccessful) {
//                                            AndroidUtils.showToast(this, "User deleted successfully")
//                                            finish()
//                                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//                                        } else {
//                                            AndroidUtils.showToast(this, "Failed to delete user from FirebaseAuth: ${authTask.exception?.message}")
//                                        }
//                                        inProgress(false)
//                                    }
//                                } else {
//                                    AndroidUtils.showToast(this, "Failed to delete user: User not logged in or mismatch")
//                                    inProgress(false)
//                                }
//                            } else {
//                                AndroidUtils.showToast(this, "Failed to delete user document: ${firestoreTask.exception?.message}")
//                                inProgress(false)
//                            }
//                        }
//                        aDialog.dismiss()
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton("No") { aDialog, _ ->
//                        aDialog.dismiss() // Simply dismiss the dialog
//                        dialog.dismiss()
//                    }
//                    .create()
//                aBuilder.show()
//            }
//            .setNegativeButton("No") { dialog, _ ->
//                dialog.dismiss() // Simply dismiss the dialog
//            }
//
//                .create()
//
//        builder.show()
//    }

    private fun showBanUserDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ban User?")
            .setMessage("Are you sure you want to ban this user?")
            .setPositiveButton("Yes") { dialog, _ ->
                inProgress(true)
                FirebaseUtils.allUserCollectionReference().document(model.userId).update("banned", true).addOnCompleteListener {
                    if (it.isSuccessful) {
                        AndroidUtils.showToast(this, "User: ${model.displayName} has been banned")
                        getUserData()
                        if (needsUpdate || changedRole || changedName || changedProfilePic || changedContactNumber) {
                            AndroidUtils.showToast(this, "Changes were rolled back")
                            needsUpdate = false
                            changedRole = false
                            changedName = false
                            changedProfilePic = false
                            changedContactNumber = false
                            showUpdateDialog()
                        }
                        dialog.dismiss()
                    } else {
                        AndroidUtils.showToast(this, "Failed to reset role")
                        inProgress(false)
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Simply dismiss the dialog
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showUnBanUserDialog() {
        FirebaseUtils.allUserCollectionReference().document(model.userId).update("banned", false).addOnCompleteListener {
            if (it.isSuccessful) {
                AndroidUtils.showToast(this, "User: ${model.displayName} has been unbanned")
                getUserData()
                if (needsUpdate || changedRole || changedName || changedProfilePic || changedContactNumber) {
                    AndroidUtils.showToast(this, "Changes were rolled back")
                    needsUpdate = false
                    changedRole = false
                    changedName = false
                    changedProfilePic = false
                    changedContactNumber = false
                    showUpdateDialog()
                }
            }
        }
    }

    private fun showRoleResetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Role")
            .setMessage("This role isn't available. Do you want to reset the role?")
            .setPositiveButton("Reset") { dialog, _ ->
                inProgress(true)
                FirebaseUtils.allUserCollectionReference().document(model.userId).update("role", "Customer").addOnCompleteListener {
                    if (it.isSuccessful) {
                        AndroidUtils.showToast(this, "Role reset successfully")
                        getUserData()
                        if (needsUpdate || changedRole || changedName || changedProfilePic || changedContactNumber) {
                            AndroidUtils.showToast(this, "Changes were rolled back")
                            needsUpdate = false
                            changedRole = false
                            changedName = false
                            changedProfilePic = false
                            changedContactNumber = false
                            showUpdateDialog()
                        }
                        dialog.dismiss()
                    } else {
                        AndroidUtils.showToast(this, "Failed to reset role")
                        inProgress(false)
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Simply dismiss the dialog
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showPasswordResetDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
            .setMessage("Are you sure you want to reset its password?")
            .setPositiveButton("Reset") { dialog, _ ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            AndroidUtils.showToast(this, "Password reset email sent to $email")
                        } else {
                            val errorMessage = task.exception?.localizedMessage ?: "An error occurred"
                            AndroidUtils.showToast(this, "Failed to send password reset email: $errorMessage")
                        }
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
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
            model = model.copy(
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
            model = model.copy(
                displayName = value,
                displayNameLowercase = value.lowercase()
            )
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


}