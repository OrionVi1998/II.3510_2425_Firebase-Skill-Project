package com.isep.firebaseSkillProject

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access a Cloud Firestore instance from your Activity

        val db = Firebase.firestore

        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("firebase-read","${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-read", "Error getting documents.", exception)
            }

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}