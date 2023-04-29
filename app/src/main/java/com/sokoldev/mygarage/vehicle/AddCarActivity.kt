package com.sokoldev.mygarage.vehicle

import android.Manifest
import android.R
import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.sokoldev.mygarage.MainActivity
import com.sokoldev.mygarage.databinding.ActivityAddCarBinding
import com.sokoldev.mygarage.network.NetworkClient
import com.sokoldev.mygarage.vehicle.data.Car
import com.sokoldev.mygarage.vehicle.data.ResponseModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*

class AddCarActivity : AppCompatActivity() {
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var binding: ActivityAddCarBinding
    private val networkClient = NetworkClient()
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    private val REQUEST_GALLERY_PERMISSION = 1
    private val REQUEST_GALLERY = 2
    private var photoUri: Uri? = null
    var downloadUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        mFirestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

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

        binding.buttonSearch.setOnClickListener {
            val vin = binding.edVinSearch.text.toString()
            if (vin != "") {
                getVinNumber(vin)
            }
        }

        binding.buttonAdd.setOnClickListener {
            binding.apply {

                progressBar.visibility = View.VISIBLE
                val make = edMake.text.toString()
                val model = edModel.text.toString()
                val year = edYear.text.toString()
                val color = edColor.text.toString()
                val insurance = edInsurance.text.toString()
                val supportNumber = edSupportNumber.text.toString()
                val vin = edVinNumber.text.toString()
                val driver = edDriver.text.toString()
                val miles = edMiles.text.toString()

                if (make == "" || model == "" || year == "" || color == "" || insurance == "" || supportNumber == "" || vin == "" || driver == "" || miles == "") {
                    Snackbar.make(
                        binding.root.rootView,
                        "Please Add All Fields",
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                    progressBar.visibility = View.GONE
                    return@apply
                }
                addVehicel(make, model, year, color, insurance, supportNumber, vin, driver, miles)
            }

        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun addVehicel(
        make: String,
        model: String,
        year: String,
        color: String,
        insurance: String,
        supportNumber: String,
        vin: String,
        driver: String,
        miles: String
    ) {
        // Get a reference to the "cars" collection

        val carsRef = mFirestore.collection("cars")

        // Generate a new, unique ID for a car document
        val carId = carsRef.document().id

        // Create a new car document with the generated ID
        val newCarRef = carsRef.document(carId)

        val car = Car(
            carId,
            make,
            model,
            year,
            color,
            vin,
            driver,
            insurance,
            supportNumber,
            miles,
            "",
            "",
            "",
            "",
            "",
            null,
            FirebaseAuth.getInstance().currentUser?.uid.toString(),
            downloadUrl,
            null
        )


        newCarRef.set(car)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(this@AddCarActivity, MainActivity::class.java))
                Log.d("TAG", "Car document created with ID: $carId")
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("TAG", "Error creating car document", e)
            }
    }


    private fun getVinNumber(vin: String) {

        val apiURL = "https://auto.dev/api/vin/$vin?apikey=ZrQEPSkKY2htZnVycWFuM0BnbWFpbC5jb20="
        networkClient.makeRequest(apiURL, object : Callback {
            override fun onResponse(call: Call, response: Response) {


                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val gson = Gson()
                    val responseModel = gson.fromJson(responseBody, ResponseModel::class.java)
                    val make = responseModel.make.name
                    val model = responseModel.model.niceName
                    val year = responseModel.years[0].year
                    val color = responseModel.colors[1].options[0].name
                    runOnUiThread {
                        Toast.makeText(
                            this@AddCarActivity, "$make $model $year $color", Toast.LENGTH_LONG
                        ).show()

                        binding.apply {
                            edMake.setText(make)
                            edModel.setText(model)
                            edYear.setText(year.toString())
                            edColor.setText(color)
                            edVinNumber.text = edVinSearch.text
                        }
                    }

                } else {
                    runOnUiThread {
                        Toast.makeText(this@AddCarActivity, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }


            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AddCarActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        })
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
            val ref = storageReference!!.child("images/" + UUID.randomUUID().toString())
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
        Glide.with(this@AddCarActivity)
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