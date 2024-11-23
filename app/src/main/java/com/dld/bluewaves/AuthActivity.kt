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
import com.dld.bluewaves.databinding.ActivityAuthBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class AuthActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityAuthBinding
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
        mBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()


        supportFragmentManager.beginTransaction()
            .replace(mBinding.fragmentContainer.id, LoginFragment())
            .commit()

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Enable the default ActionBarDrawerToggle for the hamburger menu
        toggle = ActionBarDrawerToggle(
            this, mBinding.drawerLayout, mBinding.toolbar, R.string.open_nav, R.string.close_nav
        )
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        validationSideBar(auth)

        mBinding.toolbar.setNavigationOnClickListener {
            if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                mBinding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                mBinding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }


        // Initialize the navigation view and set the listener
        mBinding.sidebarNav.setNavigationItemSelectedListener(this)

        // Close button in the sidebar
        val headerLayout = mBinding.sidebarNav.getHeaderView(0)
        val navCloseBtn = headerLayout.findViewById<ImageView>(R.id.navCloseBtn)
        navCloseBtn.setOnClickListener {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        }

    }


    companion object {
        fun changeFragment(context: AuthActivity, fragment: Fragment, addToBackStack: Boolean) {
            val transaction = context.supportFragmentManager.beginTransaction()
                .replace(context.mBinding.fragmentContainer.id, fragment)
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
        validationSideBar(FirebaseAuth.getInstance())
    }

    private fun validationSideBar(auth: FirebaseAuth) {
        val user = auth.currentUser

        if (user == null) {
            // If user is not logged in, disable certain items
            mBinding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = false
            mBinding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = false
        } else {
            // If user is logged in, enable all items
            mBinding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = true
            mBinding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = true
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_customization -> Toast.makeText(this, "Customization!", Toast.LENGTH_SHORT)
                .show()

            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }

            R.id.nav_logout -> {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                    if (it.isSuccessful) {
                        FirebaseAuth.getInstance().signOut()
                        AndroidUtils.showToast(this, "Logged out!")
                        validationSideBar(FirebaseAuth.getInstance())
                        val intent = Intent(this, SplashActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                }
            }
        }
        validationSideBar(FirebaseAuth.getInstance())
        mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}