package com.example.veluna.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.R
import com.example.veluna.databinding.FragmentPeriodCycleBinding
import java.util.Calendar

class PeriodCycleFragment : Fragment() {

    private var _binding: FragmentPeriodCycleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPeriodCycleBinding.inflate(inflater, container, false)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_birthdateFragment)
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_birthdateFragment)
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

        binding.startDateInput.setOnClickListener {
            showDatePickerDialog(binding.startDateInput)
        }

        return binding.root
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
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
