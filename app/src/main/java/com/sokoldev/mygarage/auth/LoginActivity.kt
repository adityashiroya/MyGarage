package com.sokoldev.mygarage.auth

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sokoldev.mygarage.MainActivity
import com.sokoldev.mygarage.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        binding.apply {
            buttonLogin.setOnClickListener {

                progressBar.visibility = View.VISIBLE
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()

                if (email == "") {
                    edEmail.error = "Please Enter Email Address"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }
                if (password == "") {
                    edPassword.error = "Please Enter Password "
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }

                loginUser(email, password)

            }
        }

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

    }

    private fun loginUser(email: String, password: String) {

        binding.progressBar.visibility = View.VISIBLE
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    // Login successful, get user data from Firestore
                    mFirestore.collection("users")
                        .document(uid!!)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.toObject(User::class.java)

                                val sharedPreferences =
                                    getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()

                                if (user != null) {
                                    editor.putString("userId", uid)
                                    editor.putString("userEmail", user.email)
                                    editor.apply()
                                }

                                binding.progressBar.visibility = View.GONE
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("userId", uid)
                                intent.putExtra("userEmail", user?.email)
                                startActivity(intent)
                                finish()

                            } else {
                                // User data retrieval failed, display error message
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "User data retrieval failed: " + task.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Login failed, display error message

                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Login failed: " + task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}