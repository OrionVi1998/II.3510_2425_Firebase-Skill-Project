package com.isep.firebaseSkillProject

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

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
                    Log.d("firebase-read", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("firebase-read", "Error getting documents.", exception)
            }

        setContentView(R.layout.activity_main)


        //Get the Remote Config object instance and set the minimum fetch interval to allow for frequent refreshes

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

        //start enable the dev_mode
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        // end the enable the dev_mode
        remoteConfig.setConfigSettingsAsync(configSettings)

        //Set default values from XML file
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        //Start fetching the config with callback
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
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
                displayWelcomeMessage()
            }

        // [START add_config_update_listener]
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys)

                if (configUpdate.updatedKeys.contains("welcome_message")) {
                    remoteConfig.activate().addOnCompleteListener {
                        displayWelcomeMessage()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error with code: " + error.code, error)
            }
        })
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
        private fun displayWelcomeMessage() {
            val remoteConfig = Firebase.remoteConfig

            // [START get_config_values]
            val welcomeMessage = remoteConfig[WELCOME_MESSAGE_KEY].asString()
            // [END get_config_values]
        }

        companion object {
            private const val TAG = "MainActivity"

            // Remote Config keys
            private const val WELCOME_MESSAGE_KEY = "welcome_message"
        }

    }
