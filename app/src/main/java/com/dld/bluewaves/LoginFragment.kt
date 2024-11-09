package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dld.bluewaves.databinding.FragmentLoginBinding
import com.dld.bluewaves.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

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
View.OnKeyListener{

    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and initialize binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Initialize UI components
        mBinding.emailET.onFocusChangeListener = this
        mBinding.passwordET.onFocusChangeListener = this

        // OnClick Events

        mBinding.registerNow.setOnClickListener {
            AuthActivity.changeFragment(context as AuthActivity, RegisterFragment())
        }

        mBinding.loginBtn.setOnClickListener {
            mBinding.progressBar.visibility = View.VISIBLE
            val email = mBinding.emailET.text.toString()
            val password = mBinding.passwordET.text.toString()

            if (!validateEmail()) {
                return@setOnClickListener
            }
            if (!validatePassword()) {
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(context as AuthActivity) { task ->
                    mBinding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context as AuthActivity,
                            "Logged in.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        AuthActivity.login(context as AuthActivity)
                    } else {
                        mBinding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            context as AuthActivity,
                            "Failed to login.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }


        }

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
                mBinding.progressBar.visibility = View.GONE
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(): Boolean {
        var errorMessage: String? = null
        val value: String = mBinding.passwordET.text.toString()
        if (value.isEmpty()) {
            errorMessage = "Password is required"
        } else if (value.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        }

        if (errorMessage != null) {
            mBinding.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
                mBinding.progressBar.visibility = View.GONE
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