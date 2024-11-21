package com.isep.firebaseSkillProject

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

val TAG = "FirebaseAuth"

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()

        // Access a Cloud Firestore instance from your Activity

        setContentView(R.layout.activity_main)
        val signOutButton: Button = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            signOut() // Call the signOut function
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI()
        }
        else {
            showDialog()
        }
    }

    private fun reload() {
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
        showDialog()
        updateUI()
    }

    private fun createAccount(email: String, password: String){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    currentUser = auth.currentUser
                    showNameDialog()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI()
                }
            }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(
                        baseContext,
                        "Signed In!",
                        Toast.LENGTH_SHORT,
                    ).show()
                    currentUser = auth.currentUser
                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI()
                }
            }
    }

    private fun showNameDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.enter_name, null)

        // Create the dialog and set the custom view
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val nameEditText: EditText = dialogView.findViewById(R.id.name)
        var name: String? = null
        builder.setView(dialogView)
            .setPositiveButton("Finish Creating Account") { dialog, _ ->
                // Retrieve the input text
                name = nameEditText.text.toString()
                Log.d(TAG, "Name: $name")
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                currentUser!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User profile updated.")
                        }
                    }
                updateUI()
                Toast.makeText(this, "Name Created!", Toast.LENGTH_SHORT).show()
            }
        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.sign_in, null)

        // Create the dialog and set the custom view
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val usernameEditText: EditText = dialogView.findViewById(R.id.email)
        val passwordEditText: EditText = dialogView.findViewById(R.id.password)

        builder.setView(dialogView)
            .setPositiveButton("Login") { dialog, _ ->
                // Retrieve the input text
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                signIn(username, password)
                // Do something with the input
                Toast.makeText(this, "Username: $username\nPassword: $password", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Create Account")  { dialog, _ ->

                // Retrieve the input text
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                createAccount(username, password)

                // Do something with the input
                Toast.makeText(this, "Username: $username\nPassword: $password", Toast.LENGTH_SHORT).show()
            }

        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun updateUI() {
        // Get references to the TextViews
        val textView1: TextView = findViewById(R.id.username_text) // Replace with the correct ID
        val textView2: TextView = findViewById(R.id.name_text) // Replace with the correct ID
        val user = auth.currentUser
        var name: String = "User";
        var email: String = "Not available";
        user?.let {
            name = it.displayName.toString()
            email = it.email.toString()
        }

        if (user != null) {
            // User is logged in
            textView1.text = "Welcome, ${name}!"
            textView2.text = "Your email: ${email}"
        } else {
            // User is not logged in
            textView1.text = "You are not logged in"
            textView2.text = "Please log in to access features"
        }
    }

}