package com.dld.bluewaves.view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dld.bluewaves.AnnouncementFragment
import com.dld.bluewaves.ContactFragment
import com.dld.bluewaves.TrackerFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3  // 3 Fragments: Announcement, Tracker, Contact

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnnouncementFragment()
            1 -> TrackerFragment()
            2 -> ContactFragment()
            else -> AnnouncementFragment()  // Default fragment
        }
    }
}