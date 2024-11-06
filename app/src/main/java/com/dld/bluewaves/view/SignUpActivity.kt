package com.dld.bluewaves.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.dld.bluewaves.LoginActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.data.ValidateEmailBody
import com.dld.bluewaves.databinding.ActivitySignupBinding
import com.dld.bluewaves.databinding.SignupLayoutBinding
import com.dld.bluewaves.repository.AuthRepository
import com.dld.bluewaves.utils.APIService
import com.dld.bluewaves.view_model.RegisterActivityViewModel
import com.dld.bluewaves.view_model.RegisterActivityViewModelFactory


class SignUpActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var incSignup: SignupLayoutBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout
    private lateinit var mBinding: ActivitySignupBinding
    private lateinit var mViewModel: RegisterActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivitySignupBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)

        incSignup = SignupLayoutBinding.bind(mBinding.layoutSignup.root)

        incSignup.fNameSUET.onFocusChangeListener = this
        incSignup.lNameSUET.onFocusChangeListener = this
        incSignup.usernameSUET.onFocusChangeListener = this
        incSignup.passwordSUET.onFocusChangeListener = this
        incSignup.cPasswordSUET.onFocusChangeListener = this
        incSignup.emailSUET.onFocusChangeListener = this
        incSignup.pNumberSUET.onFocusChangeListener = this
        mViewModel = ViewModelProvider(this, RegisterActivityViewModelFactory(AuthRepository(
            APIService.getService()), application)).get(RegisterActivityViewModel::class.java)
        setupObservers()

        // Initialize UI components
        val hamburgMenuBtn: ImageView = findViewById(R.id.hamburgMenuBtn)
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
//        val sidebarNav: NavigationView = findViewById(R.id.sidebar_nav)
        val signUpBackBtn: ImageView = findViewById(R.id.signUpBackBtn)
//        val signupUsernameInput: EditText = findViewById(R.id.signup_username_input)
//        val signupPasswordInput: EditText = findViewById(R.id.signup_password_input)
//        val signupConfirmPasswordInput: EditText = findViewById(R.id.signup_confirmpassword_input)
//        val signupEmailInput: EditText = findViewById(R.id.signup_email_input)
//        val signupPhoneInput: EditText = findViewById(R.id.signup_phone_input)

        signUpBackBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        // Onclick Events
        hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupObservers() {
        mViewModel.getIsLoading().observe(this) {
                incSignup.progressBar.isVisible = it
        }

        mViewModel.getErrorMessage().observe(this) {
            // firstName, lastName, username, password, confirmPassword, email, phoneNumber
            val formErrorKeys = arrayOf("firstName", "lastName", "username", "password", "confirmPassword", "email", "phoneNumber")
            val message = StringBuilder()
            it.map { entry ->
                if(formErrorKeys.contains(entry.key)){
                    when(entry.key){
                        "firstName" -> {
                            incSignup.fNameSUTil.apply{
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }
                        "lastName" -> {
                            incSignup.lNameSUTil.apply{
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }
                        "username" -> {
                            incSignup.usernameSUTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }
                        "password" -> {
                            incSignup.passwordSUTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }

                        }
                        "confirmPassword" -> {
                            incSignup.cPasswordSUTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }

                        }
                        "email" -> {
                            incSignup.emailSUTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }

                        }
                        "phoneNUmber" -> {
                            incSignup.pNumberSUTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }

                        }
                    }
                }else{
                    message.append(entry.value).append('\n')
                }
                if(message.isNotEmpty()){
                    AlertDialog.Builder(this)
                        .setIcon(R.drawable.baseline_info_24)
                        .setTitle("INFORMATION")
                        .setMessage(message)
                        .setPositiveButton("OK"){dialog, _ -> dialog!!.dismiss()}
                }
            }
        }

        mViewModel.getUser().observe(this){
            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // Validation Functions
    private fun validateFirstName(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.fNameSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "First name is required"
        }

        if (errorMessage != null) {
            incSignup.fNameSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }


        return errorMessage == null
    }

    private fun validateLastName(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.lNameSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Last name is required"
        }

        if (errorMessage != null) {
            incSignup.lNameSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null

    }

    private fun validateUsername(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.usernameSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Username is required"
        }

        if (errorMessage != null) {
            incSignup.usernameSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null

    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.passwordSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Password is required"
        } else if (value.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        }

        if (errorMessage != null) {
            incSignup.passwordSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.cPasswordSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Confirm password is required"
        } else if (value.length < 8) {
            errorMessage = "Confirm password must be at least 8 characters"
        }

        if (errorMessage != null) {
            incSignup.cPasswordSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null

    }

    private fun validatePasswordAndConfirmPassword(): Boolean {
        var errorMessage: String? = null
        val password = incSignup.passwordSUET.text.toString()
        val confirmPassword = incSignup.cPasswordSUET.text.toString()
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        }

        if (errorMessage != null) {
            incSignup.cPasswordSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateEmail(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.emailSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "Email address is invalid"
        }

        if (errorMessage != null) {
            incSignup.emailSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null

    }

    private fun validatePhoneNumber(): Boolean {
        var errorMessage: String? = null
        val value: String = incSignup.pNumberSUET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Phone number is required"
        } else if (!Patterns.PHONE.matcher(value).matches()) {
            errorMessage = "Phone number is invalid"
        } else if (value.length < 10) {
            errorMessage = "Phone number must be at least 10 digits"
        }

        if (errorMessage != null) {
            incSignup.pNumberSUTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null

    }


    override fun onClick(view: View?) {
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.fNameSUET -> {
                    if (hasFocus) {
                        if (incSignup.fNameSUTil.isErrorEnabled) {
                            incSignup.fNameSUTil.isErrorEnabled = false
                        }
                    } else {
                        validateFirstName()
                    }
                }

                R.id.lNameSUET -> {
                    if (hasFocus) {
                        if (incSignup.lNameSUTil.isErrorEnabled) {
                            incSignup.lNameSUTil.isErrorEnabled = false
                        }
                    } else {
                        validateLastName()
                    }
                }

                R.id.usernameSUET -> {
                    if (hasFocus) {
                        if (incSignup.usernameSUTil.isErrorEnabled) {
                            incSignup.usernameSUTil.isErrorEnabled = false
                        }
                    } else {
                        validateUsername()
                    }

                }

                R.id.passwordSUET -> {
                    if (hasFocus) {
                        if (incSignup.passwordSUTil.isErrorEnabled) {
                            incSignup.passwordSUTil.isErrorEnabled = false
                        }
                    } else {
                        if (validatePassword() && incSignup.cPasswordSUET.text!!.isNotEmpty() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
                            if (incSignup.cPasswordSUTil.isErrorEnabled) {
                                incSignup.cPasswordSUTil.isErrorEnabled = false
                            }
                            incSignup.cPasswordSUTil.apply {
                                setStartIconDrawable(R.drawable.blue_check_circle)
                                setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                            }
                        }
                    }

                }

                R.id.cPasswordSUET -> {
                    if (hasFocus) {
                        if (incSignup.cPasswordSUTil.isErrorEnabled) {
                            incSignup.cPasswordSUTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            if (incSignup.passwordSUTil.isErrorEnabled) {
                                incSignup.passwordSUTil.isErrorEnabled = false
                            }
                            incSignup.passwordSUTil.apply {
                                setStartIconDrawable(R.drawable.blue_check_circle)
                                setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                            }
                        }
                    }

                }

                R.id.emailSUET -> {
                    if (hasFocus) {
                        if (incSignup.emailSUTil.isErrorEnabled) {
                            incSignup.emailSUTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateEmail()) {
                            mViewModel.validateEmailAddress(ValidateEmailBody(incSignup.emailSUET.text!!.toString()))
                        }
                    }

                }

                R.id.pNumberSUET -> {
                    if (hasFocus) {
                        if (incSignup.pNumberSUTil.isErrorEnabled) {
                            incSignup.pNumberSUTil.isErrorEnabled = false
                        }
                    } else {
                        validatePhoneNumber()
                    }

                }
            }
        }
    }

    override fun onKey(view: View?, event: Int, keyEvent: KeyEvent?): Boolean {
        return false
    }
}