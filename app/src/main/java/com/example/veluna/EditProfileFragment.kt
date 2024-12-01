package com.example.veluna

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileFragment : Fragment() {

    // UI Elements
    private lateinit var etUsername: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangeProfilePicture: Button
    private lateinit var btnUpdateProfile: Button
    private lateinit var profileName: TextView

    // Firebase references
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private var updatedPhotoUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Initialize UI elements
        etUsername = view.findViewById(R.id.etUsername)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnChangeProfilePicture = view.findViewById(R.id.btnChangeProfilePicture)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        profileName = view.findViewById(R.id.ProfileName)

        // Load user data from Firestore
        loadUserData()

        // Button actions
        btnChangeProfilePicture.setOnClickListener {
            openCamera()
        }
        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        return view
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                // Display captured image in ImageView
                imgProfile.setImageBitmap(photo)
                // Upload captured image to Firebase
                uploadPhotoToFirebase(photo)
            } else {
                Log.e("Camera", "Failed to capture photo")
                Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPhotoToFirebase(bitmap: Bitmap) {
        val currentUserId = userId // Get the current userId value to avoid multiple calls to the getter
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "profile_pictures/$currentUserId.jpg"
        val photoRef = storage.child(fileName)

        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        photoRef.putBytes(data)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    val photoUrl = uri.toString()

                    // Save photo URL to Firestore
                    savePhotoUrlToFirestore(currentUserId, photoUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun savePhotoUrlToFirestore(userId: String, photoUrl: String) {
        val updatedData = mapOf("photoUrl" to photoUrl)

        db.collection("users").document(userId)
            .set(updatedData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Photo URL saved to Firestore!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save photo URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile() {
        val currentUserId = userId
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etUsername.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val birthdate = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (name.isEmpty() || phoneNumber.isEmpty() || birthdate.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = hashMapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "birthdate" to birthdate,
            "password" to password,
            "photoUrl" to updatedPhotoUrl
        )

        db.collection("users").document(currentUserId)
            .set(updatedData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editprofileFragment_to_profileFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadUserData() {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val birthdate = document.getString("birthdate") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""

                    // Update UI
                    etUsername.setText(name)
                    etPhoneNumber.setText(phoneNumber)
                    etEmail.setText(birthdate)
                    etPassword.setText("********") // Masked password
                    profileName.text = name

                    // Load image into ImageView
                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.person)
                    }
                } else {
                    Toast.makeText(requireContext(), "Data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
