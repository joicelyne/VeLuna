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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class WelcomePageFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNavigation()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val loginButton = view.findViewById<Button>(R.id.loginButton) //
        val signUpButton = view.findViewById<Button>(R.id.signUpButton)

        // Event ketika tombol "Login" diklik
        loginButton.setOnClickListener {
            // Navigasi ke halaman Login
            Log.d("WelcomePageFragment", "Login button clicked")
            findNavController().navigate(R.id.action_WelcomePageFragment_to_LoginFragment)
        }

        // Event ketika tombol "Sign Up" diklik
        signUpButton.setOnClickListener {
            // Navigasi ke halaman Sign Up
            Log.d("WelcomePageFragment", "Sign Up button clicked")
            findNavController().navigate(R.id.action_WelcomePageFragment_to_SignUpFragment)
        }
    }

}
