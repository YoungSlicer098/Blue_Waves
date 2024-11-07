package com.dld.bluewaves

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivityLoginBinding
import com.dld.bluewaves.databinding.ActivityRegisterBinding
import com.dld.bluewaves.databinding.LoginLayoutBinding
import com.dld.bluewaves.databinding.RegisterLayoutBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener  {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var incLog: LoginLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: ToolbarBinding

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
        mBinding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)

        incLog = LoginLayoutBinding.bind(mBinding.layoutLogin.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        auth = Firebase.auth


        // Onclick Events

        incLog.registerNow.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        incLog.loginBtn.setOnClickListener{

            incLog.progressBar.visibility = View.VISIBLE
            val email = incLog.emailET.text.toString()
            val password = incLog.passwordET.text.toString()

            if (!validateEmail()) {
                return@setOnClickListener
            }
            if (!validatePassword()) {
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    incLog.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Logged in.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        incLog.progressBar.visibility = View.GONE
                        Toast.makeText(
                            baseContext,
                            "Failed to login.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
    }



    private fun validateEmail(): Boolean {
        var errorMessage: String? = null
        val value: String = incLog.emailET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "Email address is invalid"
        }

        if (errorMessage != null) {
            incLog.emailTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        val value: String = incLog.passwordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Password is required"
        } else if (value.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        }

        if (errorMessage != null) {
            incLog.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.emailET -> {
                    if (hasFocus) {
                        if (incLog.emailTil.isErrorEnabled) {
                            incLog.emailTil.isErrorEnabled = false
                        }
                    } else {
                        validateEmail()
                    }
                }
                R.id.passwordET -> {
                    if (hasFocus) {
                        if (incLog.passwordTil.isErrorEnabled) {
                            incLog.passwordTil.isErrorEnabled = false
                        } else {
                            validatePassword()
                        }
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, event: Int, keyEvent: KeyEvent?): Boolean {
        TODO("Not yet implemented")
    }
}
