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
import com.dld.bluewaves.databinding.ActivityMainBinding
import com.dld.bluewaves.databinding.AnnouncementLayoutBinding
import com.dld.bluewaves.databinding.TabbarBinding
import com.dld.bluewaves.databinding.ToolbarBinding
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
        incAnnouncement = AnnouncementLayoutBinding.bind(mBinding.layoutAnnouncement.root)
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        val user = auth.currentUser
        if(user == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }else{
            incAnnouncement.user.text = "Hello, ${user.email}"
        }

        incAnnouncement.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        // Onclick Events
        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
    }
}