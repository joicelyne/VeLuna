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
import com.google.firebase.database.FirebaseDatabase

class EditWeightHeightFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_weight_height, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        editTextWeight = view.findViewById(R.id.editTextWeight)
        editTextHeight = view.findViewById(R.id.editTextHeight)
        saveButton = view.findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val weight = editTextWeight.text.toString().trim()
            val height = editTextHeight.text.toString().trim()

            if (weight.isEmpty() || height.isEmpty()) {
                Toast.makeText(requireContext(), "Weight and Height are required", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(weight, height)
            }
        }

        return view
    }

    private fun saveUserData(weight: String, height: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("EditWeightHeightFragment", "User not authenticated")
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.getReference("users").child(userId)

        val userData = mapOf(
            "weight" to weight,
            "height" to height
        )

        userRef.updateChildren(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Log.e("EditWeightHeightFragment", "Failed to save data", task.exception)
                Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}