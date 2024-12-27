package com.dld.bluewaves

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dld.bluewaves.databinding.FragmentTrackerBinding
import com.google.firebase.auth.FirebaseAuth


class TrackerFragment : Fragment() {

    private var _binding: FragmentTrackerBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and initialize binding
        _binding = FragmentTrackerBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()


        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }
}