package com.example.veluna.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.veluna.MainActivity
import com.example.veluna.R
import com.example.veluna.UserInputOnboardViewModel
import com.example.veluna.databinding.FragmentPeriodCycleBinding
import java.util.Calendar

class PeriodCycleFragment : Fragment() {

    private lateinit var viewModel: UserInputOnboardViewModel
    private var _binding: FragmentPeriodCycleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPeriodCycleBinding.inflate(inflater, container, false)

        // Get ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(UserInputOnboardViewModel::class.java)

        // Set up button click listeners
        binding.nextButton.setOnClickListener {
            if (validateInput()) {
                saveInputData()
                findNavController().navigate(R.id.action_to_birthdateFragment)
            }
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_birthdateFragment)
        }

        // Back button and related click listeners
        binding.backContainer.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backButton.setOnClickListener {
            onBackButtonClicked(it)
        }

        binding.backText.setOnClickListener {
            onBackButtonClicked(it)
        }

        // Start Date input with date picker
        binding.startDateInput.setOnClickListener {
            showDatePickerDialog(binding.startDateInput)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()

    }

    private fun validateInput(): Boolean {
        val startDate = binding.startDateInput.text.toString()
        val periodLength = binding.periodLengthInput.text.toString()
        val cycleLength = binding.cycleLengthInput.text.toString()

        return when {
            startDate.isEmpty() -> {
                Toast.makeText(activity, "Please select a start date", Toast.LENGTH_SHORT).show()
                false
            }
            periodLength.isEmpty() -> {
                Toast.makeText(activity, "Please enter the period length", Toast.LENGTH_SHORT).show()
                false
            }
            cycleLength.isEmpty() -> {
                Toast.makeText(activity, "Please enter the cycle length", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveInputData() {
        // Save the data to the ViewModel
        viewModel.userInput.startDate = binding.startDateInput.text.toString()
        viewModel.userInput.periodLength = binding.periodLengthInput.text.toString().toIntOrNull()
        viewModel.userInput.cycleLength = binding.cycleLengthInput.text.toString().toIntOrNull()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}