package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dld.bluewaves.databinding.ActivityBaseDrawerBinding
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.TabbarBinding
import com.dld.bluewaves.utils.AndroidUtils
import com.dld.bluewaves.utils.DrawerUtils
import com.dld.bluewaves.utils.FirebaseUtils
import com.dld.bluewaves.view.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityBaseDrawerBinding
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var tabbar: TabbarBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDrawerBinding.inflate(layoutInflater)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseContent.addView(mBinding.root)
        tabbar = TabbarBinding.bind(mBinding.tabbar.root)
        auth = FirebaseAuth.getInstance()

        DrawerUtils.setupDrawer(this, binding, binding.toolbar)

        // If not authenticated go back to the auth activity
        val user = auth.currentUser
        if (user == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            this.finish()
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

        val targetFragment =
            intent.getIntExtra("TARGET_FRAGMENT", -1) // Default: -1 (no fragment specified)
        if (targetFragment in 0..2) {
            mBinding.viewPager.setCurrentItem(
                targetFragment,
                false
            ) // Navigate to the desired fragment
            tabbar.tabLayout.selectTab(tabbar.tabLayout.getTabAt(targetFragment))
        }

        getFCMToken()


        tabbar.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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


    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                FirebaseUtils.currentUserDetails().update("fcmToken", token)
            }
        }
    }
}