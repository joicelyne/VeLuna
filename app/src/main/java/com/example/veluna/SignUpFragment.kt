package com.example.veluna

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input_field)
        val backButton = view.findViewById<Button>(R.id.back_button)
        val btnLoginHere = view.findViewById<Button>(R.id.btn_login_here)

        // Tombol kembali
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Tombol untuk navigasi ke LoginFragment
        btnLoginHere.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
        }

        auth = FirebaseAuth.getInstance()

        // Tombol untuk Sign Up
        val signUpButton = view.findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mendaftarkan pengguna di Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUp", "User berhasil didaftarkan")
                        val userId = auth.currentUser?.uid

                        // Simpan data pengguna ke Firestore
                        if (userId != null) {
                            saveUserToFirestore(userId, email, password)
                        }
                    } else {
                        Log.w("SignUp", "Gagal mendaftar: ", task.exception)
                        Toast.makeText(requireContext(), "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveUserToFirestore(userId: String, email: String, password: String) {
        val hashedPassword = hashPassword(password)
        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "password" to hashedPassword,
            "name" to "",
            "phoneNumber" to "",
            "birthdate" to "",
            "startDate" to "",
            "lastPeriod" to "",
            "periodLength" to null,
            "cycleLength" to null,
            "photoUrl" to ""
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("SignUp", "Data pengguna berhasil disimpan di Firestore")
                Toast.makeText(requireContext(), "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()

                // Navigasi ke halaman onboarding
                findNavController().navigate(R.id.action_SignUpFragment_to_velunaOnboardFragment)
            }
            .addOnFailureListener { e ->
                Log.e("SignUp", "Gagal menyimpan data pengguna di Firestore: ", e)
                Toast.makeText(requireContext(), "Gagal menyimpan data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

}
