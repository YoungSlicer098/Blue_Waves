package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dld.bluewaves.databinding.ActivityBaseDrawerBinding
import com.dld.bluewaves.utils.DrawerUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class AuthActivity : AppCompatActivity(){

    private lateinit var binding: ActivityBaseDrawerBinding
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        DrawerUtils.setupDrawer(this, binding, binding.toolbar)

        supportFragmentManager.beginTransaction()
            .replace(binding.baseContent.id, LoginFragment())
            .commit()
    }


    companion object {
        fun changeFragment(context: AuthActivity, fragment: Fragment, addToBackStack: Boolean) {
            val transaction = context.supportFragmentManager.beginTransaction()
                .replace(context.binding.baseContent.id, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

            if (addToBackStack) {
                transaction.addToBackStack(null) // Add to back stack if true
            }

            transaction.commit()
        }

        fun backPressed(context: AuthActivity) {
            context.onBackPressed()
        }

        fun login(context: AuthActivity) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            context.finish()
        }

    }

    override fun onResume() {
        super.onResume()
        DrawerUtils.validationSideBar(binding ,FirebaseAuth.getInstance())
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}