package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.TabbarBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.view.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var tabbar: TabbarBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        tabbar = TabbarBinding.bind(mBinding.tabbar.root)
        auth = FirebaseAuth.getInstance()

        // If not authenticated go back to the auth activity
        val user = auth.currentUser
        if(user == null){
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        // Initializing the toolbar and the sidebar drawer
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Enable the default ActionBarDrawerToggle for the hamburger menu
        toggle = ActionBarDrawerToggle(
            this, mBinding.drawerLayout, mBinding.toolbar, R.string.open_nav, R.string.close_nav
        )
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Check if the user is logged in and update the navigation drawer accordingly
        validationSideBar(auth)

        // Toolbar's Menu Button Open and Close Sidebar
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

        // Set up ViewPager2 with a FragmentStateAdapter
        viewPagerAdapter = ViewPagerAdapter(this)
        mBinding.viewPager.adapter = viewPagerAdapter

        // Connect ViewPager2 with TabLayout
        TabLayoutMediator(tabbar.tabLayout, mBinding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Announcement"
                1 -> tab.text = "Tracker"
                2 -> tab.text = "Contact"
            }
        }.attach()


        tabbar.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val newFragmentPosition = tab?.position ?: return
                mBinding.viewPager.setCurrentItem(newFragmentPosition, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                return
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                return
            }
        })
    }

    companion object {
        fun logout(context: MainActivity) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, AuthActivity::class.java)
            context.startActivity(intent)
            context.finish()
        }

        fun searchUserActivity(context: MainActivity) {
            val intent = Intent(context, SearchUserActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
        }
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
                AndroidUtils.showToast(this, "Logged out!")
                validationSideBar(FirebaseAuth.getInstance())
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