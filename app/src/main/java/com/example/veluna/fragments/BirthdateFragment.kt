package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.veluna.R
import com.example.veluna.UserInputOnboardViewModel
import com.example.veluna.databinding.FragmentBirthdateBinding
import java.text.SimpleDateFormat
import java.util.*

class BirthdateFragment : Fragment() {

    private lateinit var viewModel: UserInputOnboardViewModel
    private var _binding: FragmentBirthdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdateBinding.inflate(inflater, container, false)

        // Get ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(UserInputOnboardViewModel::class.java)

        // Set up navigation
        binding.nextButton.setOnClickListener {
            saveBirthdate()
            findNavController().navigate(R.id.action_to_userInfoFragment)
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_userInfoFragment)
        }

        // Back button functionality
        binding.backContainer.setOnClickListener { onBackButtonClicked(it) }
        binding.backButton.setOnClickListener { onBackButtonClicked(it) }
        binding.backText.setOnClickListener { onBackButtonClicked(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up DatePicker
        binding.datePicker.minDate = System.currentTimeMillis() - 100L * 365 * 24 * 60 * 60 * 1000 // 100 years ago
        binding.datePicker.maxDate = System.currentTimeMillis() // Today

        // Optionally set the initial birthdate if already available in ViewModel
        viewModel.userInput.birthdate?.let { birthdate ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(birthdate)
            date?.let { binding.datePicker.updateDate(date.year + 1900, date.month, date.date) }
        }
    }

    // Save selected date into ViewModel
    private fun saveBirthdate() {
        val day = binding.datePicker.dayOfMonth
        val month = binding.datePicker.month
        val year = binding.datePicker.year
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        // Format selected date and save in ViewModel
        val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        viewModel.userInput.birthdate = selectedDate
    }

    // Handle back button clicks
    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}