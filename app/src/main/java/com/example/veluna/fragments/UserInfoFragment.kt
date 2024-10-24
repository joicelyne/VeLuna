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
import com.example.veluna.databinding.FragmentUserInfoBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoFragment : Fragment() {

    private lateinit var viewModel: UserInputOnboardViewModel
    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        binding.backContainer.setOnClickListener { onBackButtonClicked(it) }
        binding.backButton.setOnClickListener { onBackButtonClicked(it) }
        binding.backText.setOnClickListener { onBackButtonClicked(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(UserInputOnboardViewModel::class.java)

        binding.nextButton.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val phoneNumber = binding.editTextPhoneNumber.text.toString()

            if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                viewModel.userInput.name = name
                viewModel.userInput.phoneNumber = phoneNumber

                // Save data to Firestore
                saveDataToFirestore()

                // Navigate to the next fragment
                findNavController().navigate(R.id.action_to_onBoardSuccessFragment)
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDataToFirestore() {
        val userData = viewModel.userInput

        db.collection("users")
            .document(userData.phoneNumber ?: "")
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun onBackButtonClicked(view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}