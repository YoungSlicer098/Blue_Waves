package com.dld.bluewaves
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//
//class LoginActivity2 : AppCompatActivity() {
//
//    lateinit var toggle: ActionBarDrawerToggle
//    private lateinit var navBarLayout: RelativeLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_login2)
//
//        // Initialize UI components
//        val hamburgMenuBtn: ImageView = findViewById(R.id.hamburgMenuBtn)
//        val signUpBtn: Button = findViewById(R.id.startBtn)
//
//        // Onclick Events
//
//        signUpBtn.setOnClickListener {
//            val intent = Intent(this, SignUpActivity2::class.java)
//            startActivity(intent)
//        }
//        hamburgMenuBtn.setOnClickListener {
//            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//}