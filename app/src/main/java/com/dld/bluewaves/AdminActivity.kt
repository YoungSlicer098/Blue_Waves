package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dld.bluewaves.databinding.ActivityAdminBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class AdminActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth


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
        mBinding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mBinding.userInfoContainer.setOnClickListener {
            val intent = Intent(this, AdminSearchUserActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        mBinding.wifiInfoContainer.setOnClickListener {
            AndroidUtils.showToast(this, "Coming soon!")
//            val intent = Intent(this, AdminSearchWifiActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }
        })

    }
}