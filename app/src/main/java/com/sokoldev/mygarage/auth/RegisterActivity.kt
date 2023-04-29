package com.sokoldev.mygarage.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sokoldev.mygarage.MainActivity
import com.sokoldev.mygarage.databinding.ActivityRegisterBinding
import com.sokoldev.mygarage.notification.NotificationWorker
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isNight = sharedPreferences.getBoolean("isNight", false)
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            12, TimeUnit.HOURS
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "notificationWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWorkRequest
            )





        mAuth = FirebaseAuth.getInstance()
        val current = mAuth.currentUser
        if (current != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        mFirestore = FirebaseFirestore.getInstance()

        binding.apply {
            buttonRegister.setOnClickListener {

                progressBar.visibility = View.VISIBLE
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()
                val cpasssword = edPasswordConfirm.text.toString()

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
                if (cpasssword == "") {
                    edPasswordConfirm.error = "Please Enter Password Again"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }

                if (cpasssword != password) {
                    edPassword.error = "Password Don't Match"
                    progressBar.visibility = View.GONE
                    return@setOnClickListener
                }

                createUser(email, password)

            }

            buttonLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }


    }

    private fun createUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    // Registration successful, create user object and save to Firestore
                    val user = uid?.let { User(uid, email) }
                    if (user != null) {
                        mFirestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // User data saved to Firestore, go to main activity

                                    val sharedPreferences =
                                        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()

                                    editor.putString("userId", uid)
                                    editor.putString("userEmail", user.email)
                                    editor.apply()

                                    binding.progressBar.visibility = View.GONE
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("userId", uid)
                                    intent.putExtra("userEmail", user.email)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    // User data save failed, display error message
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this,
                                        "User data save failed: " + task.exception?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    // Registration failed, display error message
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Registration failed: " + task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}