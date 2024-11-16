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
    private val db = Firebase.firestore
    private var notes = emptyArray<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        getNotes()
        // Access a Cloud Firestore instance from your Activity



        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getNotes() {
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                notes =
                    result
                        .map { document -> Note(document.id, document.data["content"].toString()) }
                        .toTypedArray()

                for (document in result) {
                    Log.d("firebase-getNotes", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-getNotes", "Error getting documents.", exception)
            }
    }

    private fun addNote(noteContent: String) {
        db.collection("notes")
            .add(hashMapOf("content" to noteContent))
            .addOnSuccessListener { result ->
                notes += Note(result.id, noteContent)
                Log.d("firebase-addNote", "Added successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-addNote", "Error adding", exception)
            }
    }

    private fun removeNotes(noteId: String) {
        db.collection("notes")
            .document(noteId)
            .delete()
            .addOnSuccessListener { result ->
                notes = notes.filter { n -> n.id !== noteId }.toTypedArray()
                Log.d("firebase-removeNotes", "Deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-removeNotes", "Error deleting", exception)
            }
    }
}

class Note(val id: String, val content: String)