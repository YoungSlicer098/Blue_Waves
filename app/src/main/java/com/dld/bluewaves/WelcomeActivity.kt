package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dld.bluewaves.databinding.ActivityBaseDrawerBinding
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.ActivityWelcomeBinding
import com.dld.bluewaves.databinding.WelcomeLayoutBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.DrawerUtils
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class WelcomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBaseDrawerBinding
    private lateinit var incWelcome: WelcomeLayoutBinding

    private var backPressedTime: Long = 0
    private val backPressInterval = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDrawerBinding.inflate(layoutInflater)
        incWelcome = WelcomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseContent.addView(incWelcome.root)

        DrawerUtils.setupDrawer(this, binding, binding.toolbar)

        // Start button click listener to launch AuthActivity
        incWelcome.startBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < backPressInterval) {
                // If the back button is pressed again within the interval, exit the app
                super.onBackPressed()
            } else {
                // Show the Toast message and update the backPressedTime
                backPressedTime = currentTime
                AndroidUtils.showToast(this, "Are you sure you want to exit?")
            }
        }
    }
}