package com.example.veluna.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.MainActivity
import com.example.veluna.R
import com.example.veluna.databinding.FragmentPeriodCycleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class PeriodCycleFragment : Fragment() {

    private var _binding: FragmentPeriodCycleBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeriodCycleBinding.inflate(inflater, container, false)

        // Tombol Next
        binding.nextButton.setOnClickListener {
            saveInputData()
        }

        // Tombol Skip
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_birthdateFragment)
        }

        // Tombol Back
        binding.backContainer.setOnClickListener { onBackButtonClicked(it) }
        binding.backButton.setOnClickListener { onBackButtonClicked(it) }
        binding.backText.setOnClickListener { onBackButtonClicked(it) }

        // Input Start Date dengan DatePicker
        binding.startDateInput.setOnClickListener {
            showDatePickerDialog(binding.startDateInput)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()
    }

    private fun saveInputData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = binding.startDateInput.text.toString()
        val periodLength = binding.periodLengthInput.text.toString().toIntOrNull()
        val cycleLength = binding.cycleLengthInput.text.toString().toIntOrNull()

        if (startDate.isEmpty() || periodLength == null || cycleLength == null) {
            Toast.makeText(requireContext(), "Harap isi semua data dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = mapOf(
            "userId" to userId,
            "startDate" to (startDate.ifEmpty { "0000-00-00" }),
            "periodLength" to (periodLength ?: 0),
            "cycleLength" to (cycleLength ?: 0)
        )

        db.collection("users").document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge()) // Merge digunakan
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_to_birthdateFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Gunakan format tanggal konsisten: yyyy-MM-dd
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
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
