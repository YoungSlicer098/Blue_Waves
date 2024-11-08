package com.dld.bluewaves

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivityRegisterBinding
import com.dld.bluewaves.databinding.RegisterLayoutBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var incReg: RegisterLayoutBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout
    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var toolbar: ToolbarBinding
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityRegisterBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)

        incReg = RegisterLayoutBinding.bind(mBinding.layoutRegister.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        auth = Firebase.auth

        // Initialize UI components
        incReg.emailET.onFocusChangeListener = this
        incReg.passwordET.onFocusChangeListener = this
        incReg.cPasswordET.onFocusChangeListener = this

        // OnClick Events
        incReg.BackBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        incReg.loginNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }

        incReg.submitBtn.setOnClickListener {
            incReg.progressBar.visibility = View.VISIBLE
            val email = incReg.emailET.text.toString()
            val password = incReg.passwordET.text.toString()
            val confirmPassword = incReg.cPasswordET.text.toString()

            if (!validateEmail()) {
                return@setOnClickListener
            }
            if (!validatePassword()) {
                return@setOnClickListener
            }
            if (!validateConfirmPassword()) {
                return@setOnClickListener
            }
            if (!validatePasswordAndConfirmPassword()) {
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    incReg.progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Account created.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            this.finish()

                        } else {
                            incReg.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                }

        }
    }

    private fun validateEmail(): Boolean {
        var errorMessage: String? = null
        val value: String = incReg.emailET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "Email address is invalid"
        }

        if (errorMessage != null) {
            incReg.emailTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        val value: String = incReg.passwordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Password is required"
        } else if (value.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        }

        if (errorMessage != null) {
            incReg.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                incReg.progressBar.visibility = View.GONE
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(): Boolean {
        var errorMessage: String? = null
        val value: String = incReg.cPasswordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Confirm password is required"
        } else if (value.length < 8) {
            errorMessage = "Confirm password must be at least 8 characters"
        }

        if (errorMessage != null) {
            incReg.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                incReg.progressBar.visibility = View.GONE
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConfirmPassword(): Boolean {
        var errorMessage: String? = null
        val password = incReg.passwordET.text.toString()
        val confirmPassword = incReg.cPasswordET.text.toString()
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        }

        if (errorMessage != null) {
            incReg.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                incReg.progressBar.visibility = View.GONE
            }
        }

        return errorMessage == null
    }

    override fun onClick(view: View?) {
        if (view != null){
            when (view.id) {
                R.id.submitSU_btn -> {
                    if (validateEmail() && validatePassword() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
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
                        if (incReg.emailTil.isErrorEnabled) {
                            incReg.emailTil.isErrorEnabled = false
                        } else {
                            validateEmail()
                        }
                    }
                }

                R.id.passwordET -> {
                    if (hasFocus) {
                        if (incReg.passwordTil.isErrorEnabled) {
                            incReg.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validatePassword() && incReg.cPasswordET.text!!.isNotEmpty() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
                            if (incReg.cPasswordTil.isErrorEnabled) {
                                incReg.cPasswordTil.isErrorEnabled = false
                            }
                            incReg.cPasswordTil.apply {
                                setStartIconDrawable(R.drawable.blue_check_circle)
                                setStartIconTintList(ColorStateList.valueOf(android.graphics.Color.CYAN))
                            }
                        }
                    }
                }

                R.id.cPasswordET -> {
                    if (hasFocus) {
                        if (incReg.cPasswordTil.isErrorEnabled) {
                            incReg.cPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            if (incReg.passwordTil.isErrorEnabled) {
                                incReg.passwordTil.isErrorEnabled = false
                            }
                            incReg.passwordTil.apply {
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
