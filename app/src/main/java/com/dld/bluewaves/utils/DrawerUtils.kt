package com.dld.bluewaves.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewbinding.ViewBinding
import com.dld.bluewaves.AdminActivity
import com.dld.bluewaves.ProfileActivity
import com.dld.bluewaves.R
import com.dld.bluewaves.SplashActivity
import com.dld.bluewaves.databinding.ActivityBaseDrawerBinding
import com.dld.bluewaves.databinding.DialogAboutUsBinding
import com.dld.bluewaves.model.UserModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
object DrawerUtils {

    private lateinit var toggle: ActionBarDrawerToggle
    @SuppressLint("StaticFieldLeak")
    private lateinit var dialogAboutUs: DialogAboutUsBinding

    // Initializes the navigation drawer
    fun setupDrawer(
        activity: Activity,
        binding: ActivityBaseDrawerBinding,
        toolbar: Toolbar
    ) {
        val auth = FirebaseAuth.getInstance()

        // Set toolbar as the ActionBar
        if (activity is androidx.appcompat.app.AppCompatActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.apply {
                setDisplayShowTitleEnabled(false) // Optional: Show title or not
                setHomeAsUpIndicator(R.drawable.hamburger_menu_white)
                setDisplayHomeAsUpEnabled(true)
            }
        }

        // Setup Drawer Toggle
        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Toolbar navigation button behavior
        toolbar.setNavigationOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Setup Sidebar Navigation
        setupSidebarNavigation(activity, binding)

        // Validation for enabling/disabling menu items
        validationSideBar(binding, auth)
    }

    // Handles sidebar navigation item clicks
    private fun setupSidebarNavigation(activity: Activity, binding: ActivityBaseDrawerBinding) {
        val auth = FirebaseAuth.getInstance()

        binding.sidebarNav.setNavigationItemSelectedListener { item ->
            handleNavigationItemSelected(activity, binding, item, auth)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        // Close button in the header
        val headerLayout = binding.sidebarNav.getHeaderView(0)
        val navCloseBtn = headerLayout.findViewById<ImageView>(R.id.navCloseBtn)
        navCloseBtn.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    // Handles individual menu item actions
    private fun handleNavigationItemSelected(activity: Activity, binding: ActivityBaseDrawerBinding, item: MenuItem, auth: FirebaseAuth) {
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
        when (item.itemId) {
            R.id.nav_customization -> Toast.makeText(activity, "Customization!", Toast.LENGTH_SHORT).show()
            R.id.nav_profile -> {
                val intent = Intent(activity, ProfileActivity::class.java)
                activity.startActivity(intent)
                activity.overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }
            R.id.nav_admin -> {
            val intent = Intent(activity, AdminActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out_static)
            }
            R.id.nav_logout -> {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                    if (it.isSuccessful) {
                        auth.signOut()
                        Toast.makeText(activity, "Logged out!", Toast.LENGTH_SHORT).show()
                        validationSideBar(binding, auth)
                        val intent = Intent(activity, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity.startActivity(intent)
                    }
                }
            }
            R.id.nav_about_us -> {
                dialogAboutUs = DialogAboutUsBinding.inflate(activity.layoutInflater)

                AlertDialog.Builder(activity)
                    .setTitle("About Us")
                    .setView(dialogAboutUs.root)
                    .setPositiveButton("Ok") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
            }

            // Developer's Data Manager Button:
            R.id.nav_dev_btn -> {
                FirebaseUtils.allUserCollectionReference().get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = FirebaseFirestore.getInstance().batch()

                        for (document in querySnapshot.documents) {
                            val data = document.toObject(UserModel::class.java)

                            // Prepare fields to update
                            val updates = mutableMapOf<String, Any?>()

                            if (data?.contactNumber == null) {
                                updates["contactNumber"] = "" // Default empty string for contact number
                            }
                            if (data?.banned == null) {
                                updates["banned"] = false // Default value for banned
                            }

                            // If there are updates, add to the batch
                            if (updates.isNotEmpty()) {
                                batch.update(document.reference, updates)
                            }
                        }

                        // Commit the batch
                        batch.commit()
                            .addOnSuccessListener {
                                println("All documents updated successfully.")
                            }
                            .addOnFailureListener { e ->
                                println("Error updating documents: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching documents: ${e.message}")
                    }

            }


        }
        validationSideBar(binding, auth)
    }


    // Validation Sidebar: Enables/Disables items based on user authentication
    fun validationSideBar(binding: ActivityBaseDrawerBinding, auth: FirebaseAuth) {
        val user = auth.currentUser
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isVisible = false
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isVisible = false

        FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
            if (it.isSuccessful) {
                val role = it.result.getString("role")
                if (role?.lowercase() == "admin") {
                    binding.sidebarNav.menu.findItem(R.id.nav_admin).isEnabled = true
                    binding.sidebarNav.menu.findItem(R.id.nav_admin).isVisible = true
                }
                if (role?.lowercase() == "developer") {
                    binding.sidebarNav.menu.findItem(R.id.nav_admin).isEnabled = true
                    binding.sidebarNav.menu.findItem(R.id.nav_admin).isVisible = true
                    binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isEnabled = true
                    binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isVisible = true
                }
            }
        }

        if (user == null) {
            binding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = false
            binding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = false
        } else {
            binding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = true
            binding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = true
        }
    }
}
