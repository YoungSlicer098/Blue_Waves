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
import androidx.fragment.app.FragmentTransaction
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.AnnouncementLayoutBinding
import com.dld.bluewaves.databinding.TabbarBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var toolbar: ToolbarBinding
    private lateinit var tabbar: TabbarBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        tabbar = TabbarBinding.bind(mBinding.tabbar.root)
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        val user = auth.currentUser
        if(user == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        // Onclick Events
        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }

        supportFragmentManager.beginTransaction().replace(mBinding.fragmentContainer.id, AnnouncementFragment())
            .addToBackStack(null)
            .commit()

        tabbar.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                val fragment: Fragment = when (tab?.position) {
                    0 -> AnnouncementFragment()
                    1 -> TrackerFragment()
                    2 -> ContactFragment()
                    else -> AnnouncementFragment() // Default or fallback fragment
                }
                supportFragmentManager.beginTransaction().replace(mBinding.fragmentContainer.id, fragment!!).setTransition(
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
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
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
            context.finish()
        }
    }
}