package com.example.veluna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    // Variabel UI untuk layout Profile

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var profileWeight: TextView
    private lateinit var profileHeight: TextView
    private lateinit var profileBMI: TextView
    private lateinit var btnEditWeightHeight: Button

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inisialisasi elemen UI di layout profil
        profileWeight = view.findViewById(R.id.profileWeight)
        profileHeight = view.findViewById(R.id.profileHeight)
        profileBMI = view.findViewById(R.id.profileBMI)
        btnEditWeightHeight = view.findViewById(R.id.btnEditWeightHeight)

        btnEditWeightHeight.setOnClickListener {
            val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EditWeightHeightFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        loadUserData()


        // Tombol Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Ikon Edit Profile
        view.findViewById<ImageView>(R.id.imgEditIcon).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editprofileFragment)
        }

        // Tombol Edit Weight & Height
        view.findViewById<Button>(R.id.btnEditWeightHeight).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editWeightHeightFragment)
        }


        return view
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.value as? Map<String, Any> ?: return@addOnSuccessListener
            val weight = userData["weight"] as? String ?: "0"
            val height = userData["height"] as? String ?: "0"
            profileWeight.text = "$weight kg"
            profileHeight.text = "$height cm"
            updateBMI(weight.toDouble(), height.toDouble())
        }
    }

    private fun updateBMI(weight: Double, height: Double) {
        val heightInMeters = height / 100
        val bmi = weight / (heightInMeters * heightInMeters)
        profileBMI.text = "BMI: %.2f".format(bmi)
    }
}
