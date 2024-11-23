package com.dld.bluewaves

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dld.bluewaves.databinding.DialogPicturesBinding
import com.dld.bluewaves.databinding.DialogProfilePicDecisionsBinding
import com.dld.bluewaves.databinding.FragmentRegisterBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storageMetadata


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private var _binding: FragmentRegisterBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var dialogProfilePic: DialogProfilePicDecisionsBinding
    private lateinit var dialogPictures: DialogPicturesBinding
    private var userModel = UserModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        // Initialize the image picker launcher
        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null && data.data != null) {
                        selectedImageUri = data.data!!
                        AndroidUtils.setProfilePic(
                            context as AuthActivity,
                            selectedImageUri,
                            mBinding.profilePic
                        )
                        userModel.profilePic = ""
                    }
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Initialize UI components
        mBinding.displayNameET.onFocusChangeListener = this
        mBinding.emailET.onFocusChangeListener = this
        mBinding.passwordET.onFocusChangeListener = this
        mBinding.cPasswordET.onFocusChangeListener = this
        mBinding.backBtn.setOnClickListener(this)
        mBinding.loginNow.setOnClickListener(this)
        mBinding.registerBtn.setOnClickListener(this)
        mBinding.profilePic.setOnClickListener(this)


        //On Click Listeners Main Functions


        return mBinding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    private fun picturesProfilePicture() {
        dialogPictures = DialogPicturesBinding.inflate(LayoutInflater.from(context as AuthActivity))

        var selectedProfilePic: String = ""

        val alertDialog = AlertDialog.Builder(context as AuthActivity)
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
                    userModel.profilePic = selectedProfilePic
                    mBinding.profilePic.setImageResource(AndroidUtils.selectPicture(selectedProfilePic))
                    alertDialog.dismiss()
                } else {
                    AndroidUtils.showToast(context as AuthActivity, "No picture selected")
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


    private fun validateDisplayName(): Boolean {
        var errorMessage: String? = null
        val value: String = mBinding.displayNameET.text.toString()
        val specialCharactersRegex = Regex("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
        if (value.isEmpty()) {
            errorMessage = "Display name is required"
        } else if (specialCharactersRegex.containsMatchIn(value)) {
            errorMessage = "Display name must not contain special characters"
        } else if (value.length < 2) {
            errorMessage = "Display name must be at least 2 characters"
        }

        if (errorMessage != null) {
            mBinding.displayNameTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        } else {
            userModel = userModel.copy(
                displayName = value,
                displayNameLowercase = value.lowercase(),
                searchKeywords = generateSearchKeywords(value)
            )
        }


        return errorMessage == null
    }

    private fun validateEmail(): Boolean {
        var errorMessage: String? = null
        val value: String = mBinding.emailET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "Email address is invalid"
        }

        if (errorMessage != null) {
            mBinding.emailTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        } else {
            userModel = userModel.copy(
                email = value
            )
        }

        return errorMessage == null
    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        var specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
        val value: String = mBinding.passwordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Password is required"
        } else if (value.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        } else if (!Regex(".*[$specialCharacters].*").containsMatchIn(value)) {
            errorMessage =
                "Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
        }

        if (errorMessage != null) {
            mBinding.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(): Boolean {
        var errorMessage: String? = null
        var specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
        val value: String = mBinding.cPasswordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Confirm password is required"
        } else if (value.length < 8) {
            errorMessage = "Confirm password must be at least 8 characters"
        } else if (!Regex(".*[$specialCharacters].*").containsMatchIn(value)) {
            errorMessage =
                "Password must contain at least one special character [!@#\$%&*()_+=|<>?{}\\[\\]~-]"
        }

        if (errorMessage != null) {
            mBinding.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConfirmPassword(): Boolean {
        var errorMessage: String? = null
        val password = mBinding.passwordET.text.toString()
        val confirmPassword = mBinding.cPasswordET.text.toString()
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        }

        if (errorMessage != null) {
            mBinding.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                inProgress(false)
            }
        }

        return errorMessage == null
    }

    private fun inProgress(inProgress: Boolean) {
        if (inProgress) {
            mBinding.progressBar.visibility = View.VISIBLE
            mBinding.registerBtn.visibility = View.GONE
        } else {
            mBinding.progressBar.visibility = View.GONE
            mBinding.registerBtn.visibility = View.VISIBLE
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

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.profilePic -> {
                    dialogProfilePic = DialogProfilePicDecisionsBinding.inflate(LayoutInflater.from(context as AuthActivity))

                    // Create the AlertDialog
                    val alertDialog = AlertDialog.Builder(context as AuthActivity)
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

                R.id.loginNow -> {
                    AuthActivity.backPressed(context as AuthActivity)
                }

                R.id.backBtn -> {
                    AuthActivity.backPressed(context as AuthActivity)
                }

                R.id.register_btn -> {
                    inProgress(true)
                    val email = mBinding.emailET.text.toString()
                    val password = mBinding.passwordET.text.toString()

                    if (!validateDisplayName() || !validateEmail() || !validatePassword() || !validateConfirmPassword()) {
                        inProgress(false)
                        return
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(context as AuthActivity) { task ->
                            if (task.isSuccessful) {
                                val currentUser = auth.currentUser
                                if (currentUser != null) {
                                    userModel = userModel.copy(
                                        userId = currentUser.uid,
                                        role = "customer",
                                        lastSession = Timestamp.now(),
                                    )


                                    // Save user details to Firestore
                                    FirebaseUtils.createUserDetails().set(userModel)
                                        .addOnCompleteListener { firestoreTask ->
                                            if (firestoreTask.isSuccessful) {
                                                if(userModel.profilePic == "") {

                                                    val metadata = storageMetadata {
                                                        cacheControl = "public, max-age=31536000" // Cache for 1 year
                                                    }
                                                    FirebaseUtils.getCurrentProfilePicStorageRef()
                                                        .putFile(selectedImageUri, metadata).addOnCompleteListener {
                                                            inProgress(false)
                                                            if (it.isSuccessful) {
                                                                AndroidUtils.showToast(
                                                                    context as AuthActivity,
                                                                    "Account created."
                                                                )
                                                                auth.signOut()
                                                                AuthActivity.changeFragment(
                                                                    context as AuthActivity,
                                                                    LoginFragment(),
                                                                    false
                                                                )
                                                            }else{
                                                                // Rollback: delete user from Firebase Authentication
                                                                currentUser.delete()
                                                                    .addOnCompleteListener { deleteTask ->
                                                                        if (deleteTask.isSuccessful) {
                                                                            AndroidUtils.showToast(
                                                                                context as AuthActivity,
                                                                                "Registration failed. Please try again."
                                                                            )
                                                                        } else {
                                                                            AndroidUtils.showToast(
                                                                                context as AuthActivity,
                                                                                "Error during rollback. Contact support."
                                                                            )
                                                                        }
                                                                    }
                                                            }
                                                        }
                                                }else {
                                                    AndroidUtils.showToast(
                                                        context as AuthActivity,
                                                        "Account created."
                                                    )
                                                    auth.signOut()
                                                    AuthActivity.changeFragment(
                                                        context as AuthActivity,
                                                        LoginFragment(),
                                                        false
                                                    )
                                                }
                                            } else {
                                                // Rollback: delete user from Firebase Authentication
                                                currentUser.delete()
                                                    .addOnCompleteListener { deleteTask ->
                                                        if (deleteTask.isSuccessful) {
                                                            AndroidUtils.showToast(
                                                                context as AuthActivity,
                                                                "Registration failed. Please try again."
                                                            )
                                                        } else {
                                                            AndroidUtils.showToast(
                                                                context as AuthActivity,
                                                                "Error during rollback. Contact support."
                                                            )
                                                        }
                                                    }
                                            }
                                        }
                                }
                            } else {
                                inProgress(false)
                                AndroidUtils.showToast(
                                    context as AuthActivity,
                                    "Authentication failed."
                                )
                            }
                        }
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.displayNameET -> {
                    if (hasFocus) {
                        if (mBinding.displayNameTil.isErrorEnabled) {
                            mBinding.displayNameTil.isErrorEnabled = false
                        }
                    } else {
                        validateDisplayName()
                    }
                }

                R.id.emailET -> {
                    if (hasFocus) {
                        if (mBinding.emailTil.isErrorEnabled) {
                            mBinding.emailTil.isErrorEnabled = false
                        }
                    } else {
                        validateEmail()
                    }
                }

                R.id.passwordET -> {
                    if (hasFocus) {
                        if (mBinding.passwordTil.isErrorEnabled) {
                            mBinding.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        validatePassword()
                        if (validatePassword() && mBinding.cPasswordET.text!!.isNotEmpty() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
                            if (mBinding.cPasswordTil.isErrorEnabled) {
                                mBinding.cPasswordTil.isErrorEnabled = false
                            }
                            mBinding.cPasswordTil.apply {
                                setStartIconDrawable(R.drawable.blue_check_circle)
                                setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                            }
                        }
                    }
                }

                R.id.cPasswordET -> {
                    if (hasFocus) {
                        if (mBinding.cPasswordTil.isErrorEnabled) {
                            mBinding.cPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        validateConfirmPassword()
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            if (mBinding.passwordTil.isErrorEnabled) {
                                mBinding.passwordTil.isErrorEnabled = false
                            }
                            mBinding.passwordTil.apply {
                                setStartIconDrawable(R.drawable.blue_check_circle)
                                setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onKey(view: View?, event: Int, keyEvent: KeyEvent?): Boolean {
        return false
    }
}