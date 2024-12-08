package com.example.veluna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditWeightHeightFragment : Fragment() {

    // UI Elements
    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var saveButton: Button

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_weight_height, container, false)

        editTextWeight = view.findViewById(R.id.editTextWeight)
        editTextHeight = view.findViewById(R.id.editTextHeight)
        saveButton = view.findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val weight = editTextWeight.text.toString().trim()
            val height = editTextHeight.text.toString().trim()

            if (weight.isEmpty() || height.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Weight and Height are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                saveUserData(weight, height)
            }
        }

        return view
    }

    private fun saveUserData(weight: String, height: String) {
        val userId = userId
        if (userId == null) {
            Log.e("EditWeightHeightFragment", "User not authenticated")
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val weightValue = weight.toDoubleOrNull() ?: 0.0
        val heightValue = height.toDoubleOrNull() ?: 0.0
        val bmi = if (heightValue > 0) {
            val heightInMeters = heightValue / 100
            weightValue / (heightInMeters * heightInMeters)
        } else {
            0.0
        }

        val userData = mapOf(
            "weight" to weight,
            "height" to height,
            "bmi" to bmi
        )

        db.collection("users").document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Log.e("EditWeightHeightFragment", "Failed to save data", task.exception)
                    Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}