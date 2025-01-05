package com.dld.bluewaves

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivitySplashBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.BuildConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")class SplashActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(mBinding.root)

        System.setProperty("FIREBASE_APPCHECK_DEBUG_TOKEN", "B71CAEEC-62E8-412A-9494-238C7669A38F")

        // Debug Token Setup for App Check
        if (BuildConfig.DEBUG) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        }

        val userId = intent.getStringExtra("userId")
        if (FirebaseUtils.isLoggedIn()) {
            userId?.let {
                fetchUserAndStartActivities(it)
            } ?: run {
                auth.signOut()
                transitionToWelcomeScreen()
            }
        } else {
            transitionToWelcomeScreen()
        }
    }

    private fun fetchUserAndStartActivities(userId: String) {
        FirebaseUtils.allUserCollectionReference().document(userId).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val model: UserModel? = it.result.toObject(UserModel::class.java)
                    if (model != null) {
                        startMainAndChatActivities(model)
                    }
                } else {
                    Log.e("SplashActivity", "Failed to fetch user data")
                }
                finish()
            }
    }

    private fun startMainAndChatActivities(model: UserModel) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra("TARGET_FRAGMENT", 2)
        }
        startActivity(mainIntent)

        val chatIntent = Intent(this, ChatActivity::class.java).apply {
            AndroidUtils.passUserModelAsIntent(this, model)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(chatIntent)
    }

    private fun transitionToWelcomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
            finish()
        }, 1000)
    }
}