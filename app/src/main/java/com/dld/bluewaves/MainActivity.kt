package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.AnnouncementLayoutBinding
import com.dld.bluewaves.databinding.TabbarBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var toolbar: ToolbarBinding
    private lateinit var tabbar: TabbarBinding
    private lateinit var incAnnouncement: AnnouncementLayoutBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        tabbar = TabbarBinding.bind(mBinding.tabbar.root)
//        incAnnouncement = AnnouncementLayoutBinding.bind(mBinding.layoutAnnouncement.root)
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        val user = auth.currentUser
        if(user == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
//        else{
//            incAnnouncement.user.text = "Hello, ${user.email}"
//        }

//        incAnnouncement.logout.setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            this.finish()
//        }
        // Onclick Events
        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
        tabbar.tabLayout.addOnTabSelectedListener(tabbar.tabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab selected
                var fragment: Fragment? = null
                when (tab?.position) {
                    0 -> {
                        fragment = AnnouncementFragment()
                        val intent = Intent(this@MainActivity, AnnouncementFragment::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        fragment = TrackerFragment()
                        val intent = Intent(this@MainActivity, TrackerFragment::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        fragment = ContactFragment()
                        val intent = Intent(this@MainActivity, ContactFragment::class.java)
                    }
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselected
            }
        })
    }
}