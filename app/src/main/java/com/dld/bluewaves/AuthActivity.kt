package com.dld.bluewaves

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dld.bluewaves.databinding.ActivityAuthBinding
import com.dld.bluewaves.databinding.ToolbarBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAuthBinding
    private lateinit var toolbar: ToolbarBinding
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
        enableEdgeToEdge()
        mBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        toolbar = ToolbarBinding.bind(mBinding.toolbar.root)
        auth = FirebaseAuth.getInstance()


        // Onclick Events
        toolbar.hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT)
                .show()
        }

        supportFragmentManager.beginTransaction()
            .replace(mBinding.fragmentContainer.id, LoginFragment())
            .addToBackStack(null)
            .commit()

//
//        tabbar.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//
//                val fragment: Fragment = when (tab?.position) {
//                    0 -> AnnouncementFragment()
//                    1 -> TrackerFragment()
//                    2 -> ContactFragment()
//                    else -> AnnouncementFragment() // Default or fallback fragment
//                }
//                supportFragmentManager.beginTransaction().replace(mBinding.fragmentContainer.id, fragment!!).setTransition(
//                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                return
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//                return
//            }
//        })
//    }
    }

    companion object {
        fun changeFragment(context: AuthActivity, fragment: Fragment){
            context.supportFragmentManager.beginTransaction()
                .replace(context.mBinding.fragmentContainer.id, fragment!!)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
        fun login(context: AuthActivity){
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            context.finish()
        }

    }
}