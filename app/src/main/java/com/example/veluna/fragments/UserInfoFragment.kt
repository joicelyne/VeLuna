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
import com.example.veluna.databinding.FragmentUserInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoFragment : Fragment() {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        // Tombol back
        binding.backContainer.setOnClickListener { onBackButtonClicked() }
        binding.backButton.setOnClickListener { onBackButtonClicked() }
        binding.backText.setOnClickListener { onBackButtonClicked() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()

        // Tombol next untuk menyimpan data dan navigasi ke fragment berikutnya
        binding.nextButton.setOnClickListener {
            saveDataToFirestore()
        }
    }

    private fun saveDataToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.editTextName.text.toString()
        val phoneNumber = binding.editTextPhoneNumber.text.toString()

        if (name.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = mapOf(
            "userId" to userId,
            "name" to name,
            "phoneNumber" to phoneNumber
        )

        db.collection("users").document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge()) // Merge digunakan
            .addOnSuccessListener {
                Toast.makeText(context, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_to_onBoardSuccessFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onBackButtonClicked() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
