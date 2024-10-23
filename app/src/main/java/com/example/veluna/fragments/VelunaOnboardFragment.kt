package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.R
import com.example.veluna.databinding.FragmentVelunaOnboardBinding

class VelunaOnboardFragment : Fragment() {

    private var _binding: FragmentVelunaOnboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVelunaOnboardBinding.inflate(inflater, container, false)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_insertLastPeriodFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}