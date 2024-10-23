package com.example.veluna

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input_field)
        auth = FirebaseAuth.getInstance()

        // Back button
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Button untuk membuka halaman SignUp
        val btnCreate = view.findViewById<Button>(R.id.btn_signup_here)
        btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
        }

        // Button login
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Cek apakah email dan password diisi
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Coba login dengan Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("Login", "signInWithEmail:success")
                        // Jika login berhasil, navigasi ke halaman onBoardFragment
                        findNavController().navigate(R.id.action_LoginFragment_to_velunaOnboardFragment)
                    } else {
                        Log.w("Login", "signInWithEmail:failure", task.exception)
                        Toast.makeText(requireContext(), "Email or Password Wrong", Toast.LENGTH_SHORT).show()

                        // Cek apakah user ada di Firebase (untuk redirect ke SignUpFragment)
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            Toast.makeText(requireContext(), "Registration first, thank you", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_LoginFragment_to_SignUpFragment)
                        }
                    }
                }
        }
    }
}
