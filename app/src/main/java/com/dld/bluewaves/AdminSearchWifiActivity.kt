package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dld.bluewaves.databinding.ActivityAdminSearchWifiBinding
import com.dld.bluewaves.utils.FirebaseUtils

class AdminSearchWifiActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAdminSearchWifiBinding

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
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAdminSearchWifiBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


    }
}