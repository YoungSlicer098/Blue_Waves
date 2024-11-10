package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dld.bluewaves.databinding.ActivityWelcomeBinding
import com.dld.bluewaves.databinding.WelcomeLayoutBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityWelcomeBinding
    private lateinit var incWelcome: WelcomeLayoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()
        incWelcome = WelcomeLayoutBinding.bind(mBinding.layoutWelcome.root)

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

        // Start button click listener to launch AuthActivity
        incWelcome.startBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        validationSideBar(FirebaseAuth.getInstance())
    }

    fun validationSideBar(auth: FirebaseAuth) {
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
            R.id.nav_customization -> Toast.makeText(this, "Customization!", Toast.LENGTH_SHORT).show()
            R.id.nav_profile -> Toast.makeText(this, "Profile!", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                validationSideBar(FirebaseAuth.getInstance())
            }
        }
        mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }
}