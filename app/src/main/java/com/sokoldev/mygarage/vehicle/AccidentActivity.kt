package com.sokoldev.mygarage.vehicle

import android.Manifest
import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sokoldev.mygarage.MainActivity
import com.sokoldev.mygarage.databinding.ActivityAccidentBinding
import com.sokoldev.mygarage.vehicle.data.Accident
import com.sokoldev.mygarage.vehicle.data.Car
import java.io.IOException
import java.util.*

class AccidentActivity : AppCompatActivity() {
    lateinit var binding: ActivityAccidentBinding
    private lateinit var mFirestore: FirebaseFirestore
    private val REQUEST_GALLERY_PERMISSION = 1
    private val REQUEST_GALLERY = 2
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    private var photoUri: Uri? = null
    var downloadUrl: String? = ""
    var car: Car? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccidentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        mFirestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        if (intent != null) {
            car = intent.getParcelableExtra<Car>("car")

            car.let {
                binding.apply {

                    if (car?.accidentHistory != null) {
                        edHistory.setText(car!!.accidentHistory!!.history)
                        car!!.accidentHistory!!.image?.let { it1 -> showImage(it1) }
                    }

                }
            }
        }


        binding.relativeImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_GALLERY_PERMISSION
                )
            } else {
                chooseImage()
            }
        }

        binding.buttonAdd.setOnClickListener {
            val history = binding.edHistory.text.toString()

            if (history.isEmpty()) {
                Snackbar.make(it, "Please Add Accident History", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addHistory(history, downloadUrl)

        }


    }

    private fun addHistory(history: String, downloadUrl: String?) {

        binding.progressBar.visibility = View.VISIBLE
        // Get a reference to the document you want to update
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("cars").document(car?.id.toString())

        val accident = Accident(history, downloadUrl)
        val updates = hashMapOf(
            "accidentHistory" to accident
        )

        docRef.update(updates as Map<String, Any>).addOnSuccessListener {

            binding.progressBar.visibility = View.GONE
            startActivity(Intent(this@AccidentActivity, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Log.d("TAG", "Failed 0")


        }


    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null && data.data != null) {
            photoUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                uploadImage(storageReference)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(storageReference: StorageReference?) {
        if (photoUri != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            val ref = storageReference!!.child("accidentImages/" + UUID.randomUUID().toString())
            ref.putFile(photoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                        downloadUrl = uri.toString()
                        showImage(downloadUrl!!)
                        progressDialog.dismiss()
                    }).addOnFailureListener(OnFailureListener {
                        // Handle any errors here
                        Toast.makeText(this, "Error Fetching", Toast.LENGTH_SHORT)
                            .show()
                        progressDialog.dismiss()
                    })

                    Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                        .totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }

    private fun showImage(downloadUrl: String) {
        Glide.with(this@AccidentActivity)
            .load(downloadUrl)
            .into(binding.image)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}