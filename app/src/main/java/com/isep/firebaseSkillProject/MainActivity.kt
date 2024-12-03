package com.isep.firebaseSkillProject

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings


class MainActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private var notes = emptyArray<Note>()
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        getNotes()

        val addNoteButton = findViewById<Button>(R.id.addNote)
        val notesHeader = findViewById<TextView>(R.id.textView)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("firebase-remote-config", "Config params updated: $updated")
                    Toast.makeText(
                        this,
                        "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Fetch failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                val color = remoteConfig.getString("header_color").trim()
                Log.d("firebase-remote-config", "Color: $color")
                notesHeader.setBackgroundColor(Color.parseColor(color))
            }

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate : ConfigUpdate) {
                Log.d("firebase-remote-config", "Updated keys: " + configUpdate.updatedKeys);

                if (configUpdate.updatedKeys.contains("welcome_message")) {
                    remoteConfig.activate().addOnCompleteListener {
                        Toast.makeText(
                            this@MainActivity,
                            remoteConfig.getString("welcome_message").trim(),
                            Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
                if (configUpdate.updatedKeys.contains("header_color")) {
                    remoteConfig.activate().addOnCompleteListener {
                        val color = remoteConfig.getString("header_color").trim()
                        Log.d("firebase-remote-config", "Color: $color")
                        notesHeader.setBackgroundColor(Color.parseColor(color))
                    }
                }
            }

            override fun onError(error : FirebaseRemoteConfigException) {
                Log.w("firebase-remote-config", "Config update error with code: " + error.code, error)
            }
        })


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