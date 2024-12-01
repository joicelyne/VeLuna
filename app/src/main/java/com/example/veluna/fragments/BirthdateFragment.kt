package com.example.veluna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.veluna.MainActivity
import com.example.veluna.R
import com.example.veluna.databinding.FragmentBirthdateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BirthdateFragment : Fragment() {

    private var _binding: FragmentBirthdateBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdateBinding.inflate(inflater, container, false)

        // Set up navigation
        binding.nextButton.setOnClickListener {
            saveBirthdate()
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
        (activity as MainActivity).hideBottomNavigation()

        // Set up DatePicker
        binding.datePicker.minDate = System.currentTimeMillis() - 100L * 365 * 24 * 60 * 60 * 1000 // 100 years ago
        binding.datePicker.maxDate = System.currentTimeMillis() // Today

        // Load existing data
        loadBirthdate()
    }

    // Load existing birthdate from Firestore
    private fun loadBirthdate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val birthdate = document.getString("birthdate")
                    if (!birthdate.isNullOrEmpty()) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = dateFormat.parse(birthdate)
                        date?.let {
                            binding.datePicker.updateDate(it.year + 1900, it.month, it.date)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save selected birthdate to Firestore
    private fun saveBirthdate() {
        val day = binding.datePicker.dayOfMonth
        val month = binding.datePicker.month
        val year = binding.datePicker.year
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = mapOf(
            "userId" to userId,
            "birthdate" to selectedDate
        )

        db.collection("users").document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge()) // Merge digunakan
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tanggal lahir berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_to_userInfoFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
