package com.example.veluna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class EditProfileFragment : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout edit_profile.xml
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Inisialisasi elemen UI di layout edit profile
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etPassword = view.findViewById(R.id.etPassword)

        // Tombol Update Profile
        view.findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            // Lakukan pembaruan data profil di sini

            // Kembali ke ProfileFragment setelah update
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
