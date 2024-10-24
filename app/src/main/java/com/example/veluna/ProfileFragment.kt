package com.example.veluna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    // Variabel UI untuk layout Profile
    private lateinit var profileWeight: TextView
    private lateinit var profileHeight: TextView
    private lateinit var profileBMI: TextView

    // Variabel UI untuk layout Edit Profile
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText

    private var weight: Double = 47.0
    private var height: Double = 155.0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout awal (fragment_profile.xml)
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi elemen UI di layout profil
        profileWeight = view.findViewById(R.id.profileWeight)
        profileHeight = view.findViewById(R.id.profileHeight)
        profileBMI = view.findViewById(R.id.profileBMI)

        updateBMI()

        // Tombol Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Ikon Edit Profile
        view.findViewById<ImageView>(R.id.imgEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }


        return view
    }

    private fun updateBMI() {
        val heightInMeters = height / 100
        val bmi = weight / (heightInMeters * heightInMeters)
        profileWeight.text = "Weight: $weight kg"
        profileHeight.text = "Height: $height cm"
        profileBMI.text = "BMI: %.2f".format(bmi)
    }
}
