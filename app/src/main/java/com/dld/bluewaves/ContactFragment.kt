package com.dld.bluewaves

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dld.bluewaves.databinding.FragmentAnnouncementBinding
import com.dld.bluewaves.databinding.FragmentContactBinding
import com.dld.bluewaves.databinding.FragmentTrackerBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and initialize binding
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Access the current user and safely handle nullability
        val user = auth.currentUser
        mBinding.user.text = "Hello, ${user?.email ?: "Guest"}"

        // Set up logout button listener
        mBinding.logout.setOnClickListener {
            MainActivity.logout(context as MainActivity)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }
}
