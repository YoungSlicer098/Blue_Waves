package com.dld.bluewaves

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dld.bluewaves.databinding.FragmentLoginBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var isResetAllowed = true // Flag to track if the user can reset the password
    private var timer: CountDownTimer? = null
    private var remainingTimeMillis: Long = 0L

    private val sharedPreferences by lazy {
        activity?.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
    }

    private fun saveEmail(email: String, rememberMe: Boolean) {
        val editor = sharedPreferences?.edit()
        if (rememberMe) {
            editor?.putString("SAVED_EMAIL", email)
            editor?.putBoolean("REMEMBER_EMAIL", true)
        } else {
            editor?.remove("SAVED_EMAIL")
            editor?.putBoolean("REMEMBER_EMAIL", false)
        }
        editor?.apply()
    }

    private fun savePassword(password: String, rememberMe: Boolean) {
        val editor = sharedPreferences?.edit()
        if (rememberMe) {
            editor?.putString("SAVED_PASSWORD", password)
            editor?.putBoolean("REMEMBER_PASSWORD", true)
        } else {
            editor?.remove("SAVED_PASSWORD")
            editor?.putBoolean("REMEMBER_PASSWORD", false)
        }
        editor?.apply()
    }

    private fun loadSavedEmail() {
        val isRemembered = sharedPreferences?.getBoolean("REMEMBER_EMAIL", false)
        if (isRemembered == true) {
            val savedEmail = sharedPreferences?.getString("SAVED_EMAIL", "")
            mBinding.emailET.setText(savedEmail)
            mBinding.rememberEmailCheckbox.isChecked = true
        }
    }

    private fun loadSavedPassword() {
        val isRemembered = sharedPreferences?.getBoolean("REMEMBER_PASSWORD", false)
        if (isRemembered == true) {
            val savedPassword = sharedPreferences?.getString("SAVED_PASSWORD", "")
            mBinding.passwordET.setText(savedPassword)
            mBinding.rememberPasswordCheckbox.isChecked = true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()


        // Initialize UI components
        mBinding.emailET.onFocusChangeListener = this
        mBinding.passwordET.onFocusChangeListener = this
        mBinding.registerNow.setOnClickListener(this)
        mBinding.loginBtn.setOnClickListener(this)
        mBinding.forgetPassword.setOnClickListener(this)

        loadSavedEmail()
        loadSavedPassword()

        // OnClick Events


        return mBinding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
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
        }

        return errorMessage == null
    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        val specialCharacters = "[!@#$%&*()_+=|<>?{}\\[\\]~-]"
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

    private fun inProgress(inProgress: Boolean) {
        if (inProgress) {
            mBinding.progressBar.visibility = View.VISIBLE
            mBinding.loginBtn.visibility = View.GONE
        } else {
            mBinding.progressBar.visibility = View.GONE
            mBinding.loginBtn.visibility = View.VISIBLE
        }
    }

    private fun showPasswordResetDialog(email: String) {
        val builder = AlertDialog.Builder(context as AuthActivity)
        builder.setTitle("Reset Password")
            .setMessage("Are you sure you want to reset its password?")
            .setPositiveButton("Reset") { dialog, _ ->
                if (isResetAllowed) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                AndroidUtils.showToast(
                                    context as AuthActivity,
                                    "Password reset email sent to $email. You can request another reset in 5 minutes."
                                )
                                startResetTimer()
                            } else {
                                val errorMessage = task.exception?.localizedMessage ?: "An error occurred"
                                AndroidUtils.showToast(
                                    context as AuthActivity,
                                    "Failed to send password reset email: $errorMessage"
                                )
                            }
                        }
                } else {
                    AndroidUtils.showToast(context as AuthActivity, "Please wait for the timer to finish before resetting again.")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun startResetTimer() {
        isResetAllowed = false
        timer?.cancel() // Cancel any existing timer
        remainingTimeMillis = 5 * 60 * 1000 // Reset timer to 5 minutes

        timer = object : CountDownTimer(remainingTimeMillis, 1000) { // 5 minutes
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
            }

            override fun onFinish() {
                isResetAllowed = true
                remainingTimeMillis = 0L // Reset remaining time
                AndroidUtils.showToast(context as AuthActivity, "You can now reset the password.")
            }
        }.start()
    }

    private fun getFormattedTime(): String {
        val minutes = remainingTimeMillis / 1000 / 60
        val seconds = (remainingTimeMillis / 1000) % 60
        return "$minutes:${"%02d".format(seconds)}"
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.registerNow -> {
                    AuthActivity.changeFragment(context as AuthActivity, RegisterFragment(), true)
                }

                R.id.loginBtn -> {
                    inProgress(true)
                    val email = mBinding.emailET.text.toString()
                    val password = mBinding.passwordET.text.toString()
                    val rememberEmail = mBinding.rememberEmailCheckbox.isChecked
                    val rememberPassword = mBinding.rememberPasswordCheckbox.isChecked

                    if (!validateEmail() || !validatePassword()) {
                        return
                    }

                    saveEmail(email, rememberEmail)

                    savePassword(password, rememberPassword)

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(context as AuthActivity) { task ->
                            inProgress(false)
                            if (task.isSuccessful) {
                                FirebaseUtils.currentUserDetails().get().addOnCompleteListener{
                                    if (it.isSuccessful){
                                        val banned = it.result.getBoolean("banned")
                                        if (banned == true){
                                            auth.signOut()
                                            AndroidUtils.showToast(context as AuthActivity, "This user is banned.")
                                        }else{
                                            AndroidUtils.showToast(context as AuthActivity, "Logged in.")
                                            AuthActivity.login(context as AuthActivity)
                                        }
                                    }
                                }
                            } else {
                                AndroidUtils.showToast(
                                    context as AuthActivity,
                                    "Wrong password or email."
                                )
                            }
                        }

                }

                R.id.forgetPassword -> {
                    if (!isResetAllowed) {
                        AndroidUtils.showToast(context as AuthActivity, "Please wait for the timer to finish: ${getFormattedTime()}")
                    } else if (validateEmail()) {
                        val email = mBinding.emailET.text.toString()
                        lifecycleScope.launch {
                            if (!FirebaseUtils.sameEmailVerify(email)) {
                                showPasswordResetDialog(mBinding.emailET.text.toString())
                            } else {
                                AndroidUtils.showToast(context as AuthActivity, "Email does not exist.")
                            }
                        }
                    } else{
                        AndroidUtils.showToast(context as AuthActivity, "Fill up the email field.")
                    }
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
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
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, event: Int, keyEvent: KeyEvent?): Boolean {
        TODO("Not yet implemented")
    }
}