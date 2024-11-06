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

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize UI components
        val hamburgMenuBtn: ImageView = findViewById(R.id.hamburgMenuBtn)
        val startBtn: Button = findViewById(R.id.startBtn)

        startBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
//
//
//        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        hamburgMenuBtn.setOnClickListener() {
//            drawerLayout.openDrawer(navBarLayout)
//        }
//
//        sidebarNav.setNavigationItemSelectedListener {
//
//            when(it.itemId){
//
//                R.id.sidebar_settings -> Toast.makeText(applicationContext, "Clicked Settings.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_background -> Toast.makeText(applicationContext, "Clicked Background.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_background_blue -> Toast.makeText(applicationContext, "Clicked Blue.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_background_black -> Toast.makeText(applicationContext, "Clicked Black.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_background_purple -> Toast.makeText(applicationContext, "Clicked Purple.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_background_white -> Toast.makeText(applicationContext, "Clicked White.", Toast.LENGTH_SHORT).show()
//                R.id.sidebar_logout -> Toast.makeText(applicationContext, "Clicked Logout.", Toast.LENGTH_SHORT).show()
//
//            }
//
//            true
//
//        }

        // Onclick Events
        hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        if (toggle.onOptionsItemSelected(item)){
//
//            return true
//
//        }
//
//        return true
//    }

//    private fun replaceFragment(fragment : Fragment) {
//
//        val fragmentManager = supportFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.fragment_container, fragment)
//        fragmentTransaction.commit()
//
//    }

}