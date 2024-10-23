package com.example.veluna

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi Firebase di sini
        FirebaseApp.initializeApp(this)
        Log.d("MyApplication", "Firebase initialized")

    }
}