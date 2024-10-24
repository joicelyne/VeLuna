package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.veluna.MainActivity
import com.example.veluna.R
import com.example.veluna.UserInputOnboardViewModel
import com.example.veluna.databinding.FragmentInsertLastPeriodBinding
import java.text.SimpleDateFormat
import java.util.*

class InsertLastPeriodFragment : Fragment() {

    private lateinit var viewModel: UserInputOnboardViewModel
    private var _binding: FragmentInsertLastPeriodBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInsertLastPeriodBinding.inflate(inflater, container, false)

        // Get ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(UserInputOnboardViewModel::class.java)

        // Initialize the calendar date and display it
        selectedDate = binding.calendarView.date
        updateSelectedDateDisplay(selectedDate)

        // Listen to date changes in the calendar view
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        }

        // OK button to confirm date selection
        binding.okButton.setOnClickListener {
            updateSelectedDateDisplay(selectedDate)
            // Save the selected date to the ViewModel
            viewModel.userInput.lastPeriod = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
        }

        // Cancel button action
        binding.cancelButton.setOnClickListener {
            Toast.makeText(activity, "Date selection cancelled", Toast.LENGTH_SHORT).show()
        }

        // Navigate to PeriodCycleFragment when next or skip button is clicked
        binding.nextButton.setOnClickListener {
            saveSelectedDate()
            findNavController().navigate(R.id.action_to_periodCycleFragment)
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_periodCycleFragment)
        }

        // Back button functionality
        binding.backContainer.setOnClickListener { onBackButtonClicked(it) }
        binding.backButton.setOnClickListener { onBackButtonClicked(it) }
        binding.backText.setOnClickListener { onBackButtonClicked(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()

    }
    // Update the displayed selected date
    private fun updateSelectedDateDisplay(selectedDate: Long) {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date(selectedDate))
        binding.selectDate.text = dateString
    }

    // Save the selected date into ViewModel
    private fun saveSelectedDate() {
        viewModel.userInput.lastPeriod = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
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