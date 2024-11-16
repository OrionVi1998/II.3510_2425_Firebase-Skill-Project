package com.isep.firebaseSkillProject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
        setContentView(R.layout.activity_main)

        getNotes()

        val addNoteButton = findViewById<Button>(R.id.addNote)
        addNoteButton.setOnClickListener {
            val noteField = findViewById<EditText>(R.id.noteText)
            addNote(noteField.getText().toString())
            noteField.getText().clear()
        }

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

                buildNotes()
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
                buildNotes()
                Log.d("firebase-addNote", "Added successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-addNote", "Error adding", exception)
            }
    }

    private fun removeNote(noteId: String) {
        db.collection("notes")
            .document(noteId)
            .delete()
            .addOnSuccessListener { result ->
                notes = notes.filter { n -> n.id !== noteId }.toTypedArray()
                buildNotes()
                Log.d("firebase-removeNotes", "Deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-removeNotes", "Error deleting", exception)
            }
    }

    private fun buildNotes() {
        val container = findViewById<LinearLayout>(R.id.noteContainer)
        container.removeAllViews()
        for (note in notes) {
            val noteButton = Button(this)
            noteButton.width = LinearLayout.LayoutParams.MATCH_PARENT
            noteButton.text = note.content
            noteButton.setOnClickListener { removeNote(note.id) }

            container.addView(noteButton)
        }
    }
}

class Note(val id: String, val content: String)