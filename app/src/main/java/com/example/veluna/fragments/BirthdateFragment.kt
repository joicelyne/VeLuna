package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.R
import com.example.veluna.databinding.FragmentBirthdateBinding

class BirthdateFragment : Fragment() {

    private var _binding: FragmentBirthdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdateBinding.inflate(inflater, container, false)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_userInfoFragment)
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_userInfoFragment)
        }

        binding.backContainer.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backButton.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backText.setOnClickListener {
            onBackButtonClicked(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up DatePicker or Scroll Picker logic if using custom DatePicker
        binding.datePicker.minDate = System.currentTimeMillis() - 100L * 365 * 24 * 60 * 60 * 1000 // Minimum date example (100 years ago)
        binding.datePicker.maxDate = System.currentTimeMillis() // Maximum date example (today)
    }

    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
