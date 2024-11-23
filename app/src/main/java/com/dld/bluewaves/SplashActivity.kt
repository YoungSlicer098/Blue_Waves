package com.dld.bluewaves

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivitySplashBinding
import com.dld.bluewaves.model.UserModel
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        if (FirebaseUtils.isLoggedIn() && intent.extras != null) {
            //from notification
            val userId = intent.getStringExtra("userId")
            if (userId != null) {
                FirebaseUtils.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val model: UserModel? = it.result.toObject(UserModel::class.java)

                            val mainIntent = Intent(this, MainActivity::class.java)
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                .putExtra("TARGET_FRAGMENT", 2)
                            startActivity(mainIntent)
                            val intent = Intent(this, ChatActivity::class.java).apply {
                                AndroidUtils.passUserModelAsIntent(this, model!!)
                                this.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        } else {

            // Set up a delayed transition to the MainActivity
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
                finish()
            }, 1000)
        }
    }
}
