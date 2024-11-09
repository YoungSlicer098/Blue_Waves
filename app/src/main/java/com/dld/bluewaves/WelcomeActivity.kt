package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.dld.bluewaves.databinding.ActivityWelcomeBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.dld.bluewaves.databinding.WelcomeLayoutBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBarLayout: RelativeLayout
    private lateinit var mBinding: ActivityWelcomeBinding
    private lateinit var toolbar: ToolbarBinding
    private lateinit var incWelcome: WelcomeLayoutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        incWelcome = WelcomeLayoutBinding.bind(mBinding.layoutWelcome.root)

        incWelcome.startBtn.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // Onclick Events
        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }
    }
}