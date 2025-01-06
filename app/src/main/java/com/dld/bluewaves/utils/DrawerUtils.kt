package com.dld.bluewaves.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.dld.bluewaves.databinding.DialogPostFeedbackBinding
import com.dld.bluewaves.databinding.DialogPostSuggestionBinding
import com.dld.bluewaves.model.UserModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
@SuppressLint("StaticFieldLeak")
object DrawerUtils {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var dialogAboutUs: DialogAboutUsBinding
    private lateinit var dialogPostFeedbackBinding: DialogPostFeedbackBinding
    private lateinit var dialogPostSuggestionBinding: DialogPostSuggestionBinding

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

        binding.sidebarNav.menu.findItem(R.id.nav_customization).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_schedule).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_version_changelog).isEnabled = false

        // Validation for enabling/disabling menu items
        validationSideBar(binding, auth)


        if (auth.currentUser != null) {
            FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val banned = it.result.getBoolean("banned")
                    if (banned == true) {
                        AndroidUtils.showToast(activity, "This user is banned.")
                        val intent = Intent(activity, SplashActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
            }
        }
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
        when (item.itemId) {
            R.id.nav_customization -> AndroidUtils.showToast(activity, "Coming Soon!")
            R.id.nav_schedule -> AndroidUtils.showToast(activity, "Coming Soon!")
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
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Logout?")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                            if (it.isSuccessful) {
                                clearSavedRemember(activity)
                                auth.signOut()
                                Toast.makeText(activity, "Logged out!", Toast.LENGTH_SHORT).show()
                                validationSideBar(binding, auth)
                                val intent = Intent(activity, SplashActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                activity.startActivity(intent)
                            }
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss() // Simply dismiss the dialog
                    }

                val alertDialog = builder.create()
                alertDialog.show()
            }
            R.id.nav_about_us -> {
                dialogAboutUs = DialogAboutUsBinding.inflate(activity.layoutInflater)

                val dialog = AlertDialog.Builder(activity)
                    .setTitle("About Us")
                    .setView(dialogAboutUs.root)
                    .setPositiveButton("Ok") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialogAboutUs.email.setOnClickListener {
                    // Get the text from the TextView
                    val emailText = dialogAboutUs.email.text.toString()

                    // Copy the text to the clipboard
                    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Email", emailText)
                    clipboard.setPrimaryClip(clip)

                    // Show a toast message
                    AndroidUtils.showToast(activity, "Email copied to clipboard!")
                }
                dialog.show()

                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                    (activity.resources.displayMetrics.heightPixels * 0.7).toInt() // Height
                )
            }

            R.id.nav_version_changelog -> {
                AndroidUtils.showToast(activity, "Coming Soon!")
            }

            R.id.nav_feedback -> {
                dialogPostFeedbackBinding = DialogPostFeedbackBinding.inflate(activity.layoutInflater)
                fun inProgress(isVisible: Boolean) {
                    dialogPostFeedbackBinding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
                }

                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Feedback")
                    .setView(dialogPostFeedbackBinding.root)
                    .setPositiveButton("Send") { dialog, _ ->
                        inProgress(true)
                        val message = dialogPostFeedbackBinding.messageET.text.toString()
                        FirebaseUtils.addFeedback(message).addOnCompleteListener {
                            if (it.isSuccessful) {
                                AndroidUtils.showToast(activity, "Feedback sent!")
                                inProgress(false)
                                dialog.dismiss()
                            } else {
                                AndroidUtils.showToast(activity, "Failed to send the feedback.")
                                inProgress(false)
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss() // Simply dismiss the dialog
                    }

                val alertDialog = builder.create()
                alertDialog.show()
            }

            R.id.nav_suggestions -> {
                dialogPostSuggestionBinding = DialogPostSuggestionBinding.inflate(activity.layoutInflater)
                fun inProgress(isVisible: Boolean) {
                    dialogPostSuggestionBinding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
                }

                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Suggestion")
                    .setView(dialogPostSuggestionBinding.root)
                    .setPositiveButton("Send") { dialog, _ ->
                        inProgress(true)
                        val message = dialogPostSuggestionBinding.messageET.text.toString()
                        FirebaseUtils.addFeedback(message).addOnCompleteListener {
                            if (it.isSuccessful) {
                                AndroidUtils.showToast(activity, "Suggestion sent!")
                                inProgress(false)
                                dialog.dismiss() // Simply dismiss the dialog
                            } else {
                                AndroidUtils.showToast(activity, "Failed to send the Suggestion.")
                                inProgress(false)
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss() // Simply dismiss the dialog
                    }

                val alertDialog = builder.create()
                alertDialog.show()
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
                                AndroidUtils.showToast(activity, "All documents updated successfully.")
                            }
                            .addOnFailureListener { e ->
                                AndroidUtils.showToast(activity,"Error updating documents: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        AndroidUtils.showToast(activity,"Error fetching documents: ${e.message}")
                    }

            }


        }
        validationSideBar(binding, auth)
    }

    private fun clearSavedRemember(activity: Activity) {
        val editor = activity.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE).edit()
        editor.remove("SAVED_EMAIL")
        editor.putBoolean("REMEMBER_EMAIL", false)
        editor.remove("SAVED_PASSWORD")
        editor.putBoolean("REMEMBER_PASSWORD", false)
        editor.apply()
    }


    // Validation Sidebar: Enables/Disables items based on user authentication
    fun validationSideBar(binding: ActivityBaseDrawerBinding, auth: FirebaseAuth) {
        val user = auth.currentUser
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isVisible = false
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isEnabled = false
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isVisible = false


        if (user == null) {
            binding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = false
            binding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = false
            binding.sidebarNav.menu.findItem(R.id.nav_feedback).isEnabled = false
            binding.sidebarNav.menu.findItem(R.id.nav_suggestions).isEnabled = false
        } else {
            binding.sidebarNav.menu.findItem(R.id.nav_profile).isEnabled = true
            binding.sidebarNav.menu.findItem(R.id.nav_logout).isEnabled = true
            binding.sidebarNav.menu.findItem(R.id.nav_feedback).isEnabled = true
            binding.sidebarNav.menu.findItem(R.id.nav_suggestions).isEnabled = true

            FirebaseUtils.currentUserDetails().get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val role = it.result.getString("role")
                    if (role?.lowercase() == "admin") {
                        adminPermission(binding)
                    }
                    if (role?.lowercase() == "developer") {
                        developerPermission(binding)
                    }
                }
            }
        }
    }

    private fun adminPermission(binding: ActivityBaseDrawerBinding) {
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isEnabled = true
        binding.sidebarNav.menu.findItem(R.id.nav_admin).isVisible = true
    }

    private fun developerPermission(binding: ActivityBaseDrawerBinding) {
        adminPermission(binding)
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isEnabled = true
        binding.sidebarNav.menu.findItem(R.id.nav_dev_btn).isVisible = true
    }
}
