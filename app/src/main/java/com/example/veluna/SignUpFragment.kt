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


class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password_input_field)
        val backButton = view.findViewById<Button>(R.id.back_button)
        val btnloginhere = view.findViewById<Button>(R.id.btn_login_here)


        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        btnloginhere.setOnClickListener{
            findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
        }

        auth = FirebaseAuth.getInstance()
        val signUpButton = view.findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required",
                    Toast.LENGTH_SHORT).show()
            }else{
                Log.d("email", email)
                Log.d("password", password)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("Register", "RegisterWithEmail:success")
                            findNavController().navigate(R.id.action_SignUpFragment_to_LoginFragment)
                        } else {
                            Log.w("Register", "RegisterWithEmail:failure", task.exception)
                            Toast.makeText(requireContext(), "Register Failed " +
                                    "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}