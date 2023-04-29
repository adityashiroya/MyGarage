package com.sokoldev.mygarage.vehicle

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sokoldev.mygarage.MainActivity
import com.sokoldev.mygarage.databinding.ActivityCarDetailsBinding
import com.sokoldev.mygarage.utils.Global
import com.sokoldev.mygarage.utils.MonthYearPickerDialog
import com.sokoldev.mygarage.vehicle.data.Car
import com.sokoldev.mygarage.vehicle.data.MonthlyExpense
import java.text.DateFormatSymbols
import java.util.*

class CarDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarDetailsBinding
    var car: Car? = null
    var selectedMonth: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {

            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isKm = sharedPreferences.getBoolean("isKm", false)


        if (intent != null) {
            car = intent.getParcelableExtra<Car>("car")

            car.let {
                binding.apply {
                    tvMake.text = it?.make ?: ""
                    tvModel.text = it?.model ?: ""
                    tvYear.text = it?.year ?: ""
                    tvVin.text = it?.vinNumber ?: ""
                    tvInsurance.text = it?.insuranceProvider ?: ""
                    tvNumber.text = it?.supportNumber ?: ""
                    tvDrivers.text = it?.drivers ?: ""

                    tvAccident.text = it?.accidentHistory?.history ?: "None"

                    edAlignment.setText(it?.alignment ?: "")
                    edBalancing.setText(it?.balancing ?: "")
                    edGas.setText(it?.gasUsed ?: "")
                    val miles = it?.milesDriven ?: ""
                    edOil.setText(it?.lastOilChange ?: "")
                    edTire.setText(it?.lastTireChange ?: "")
                    showImage(it?.image, imageCar)

                    if (isKm) {
                        if (miles != "") {
                            val km = miles.toFloat() * 1.60934
                            edMiles.setText(km.toString())
                        }
                    } else {
                        edMiles.setText(miles)
                    }
                }
            }
        }

        binding.apply {
            edOil.setOnClickListener {
                Global.setupDatePicker(this@CarDetailsActivity, edOil)
            }
            edTire.setOnClickListener {
                Global.setupDatePicker(this@CarDetailsActivity, edTire)
            }
            edAlignment.setOnClickListener {
                Global.setupDatePicker(this@CarDetailsActivity, edAlignment)
            }
            edBalancing.setOnClickListener {
                Global.setupDatePicker(this@CarDetailsActivity, edBalancing)
            }
            accidentHistory.setOnClickListener {
                val intent = Intent(this@CarDetailsActivity, AccidentActivity::class.java)
                intent.putExtra("car", car)
                startActivity(intent)
            }

            edDate.setOnClickListener {
                val picker = MonthYearPickerDialog()
                picker.setListener(object : MonthYearPickerDialog.OnDateSetListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDateSet(year: Int, monthOfYear: Int) {
                        edDate.setText("$monthOfYear-$year")
                    }
                })
                picker.show(supportFragmentManager, "MonthYearPickerDialog")
            }

            buttonSave.setOnClickListener {
                if (edDate.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@CarDetailsActivity, "Please add date first", Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (edExpense.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@CarDetailsActivity, "Please add expense first", Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                updateVehicleData(car)
            }

            buttonCancel.setOnClickListener {
                startActivity(Intent(this@CarDetailsActivity, MainActivity::class.java))
                finish()
            }

            buttonDelete.setOnClickListener {
                deleteCar()
            }

        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun updateVehicleData(car: Car?) {

        binding.progressBar.visibility = View.VISIBLE
        // Get a reference to the document you want to update
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("cars").document(car?.id.toString())

        var listExpense = ArrayList<MonthlyExpense>()
        val monthYear = binding.edDate.text.toString()
        val parts = monthYear.split("-")
        val month = parts[0].toInt()
        val year = parts[1].toInt()

        val monthName = DateFormatSymbols(Locale.getDefault()).months[month - 1]


        val monthlyExpense = MonthlyExpense(
            car?.id, monthName, year.toString(), binding.edExpense.text.toString()
        )
        listExpense.add(monthlyExpense)

        val updates = hashMapOf(
            "milesDriven" to binding.edMiles.text.toString(),
            "gasUsed" to binding.edGas.text.toString(),
            "lastOilChange" to binding.edOil.text.toString(),
            "lastTireChange" to binding.edTire.text.toString(),
            "alignment" to binding.edAlignment.text.toString(),
            "balancing" to binding.edBalancing.text.toString(),
            "monthlyExpenses" to listExpense
        )


// Update the document with the new values
        docRef.update(updates as Map<String, Any>).addOnSuccessListener {
            val expenseRef = db.collection("monthlyExpenses")

            val expenseId = expenseRef.document().id

            val newRef = expenseRef.document(expenseId)

            newRef.set(monthlyExpense).addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(this@CarDetailsActivity, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Log.d("TAG", "Failed 1")
            }
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Log.d("TAG", "Failed 0")


        }
    }

    private fun showImage(downloadUrl: String?, image: AppCompatImageView) {
        Glide.with(this@CarDetailsActivity).load(downloadUrl).into(image)
    }


    private fun deleteCar() {
        binding.progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val carRef = db.collection("cars")
        val expenseRef = db.collection("monthlyExpenses")

        val carID = car?.id

        val queryCar: Query = carRef.whereEqualTo("id", carID)
        val queryExpense: Query = expenseRef.whereEqualTo("carId", carID)

        queryExpense.get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (doc in it) {
                    val ref = doc.reference
                    ref.delete().addOnSuccessListener {
                        Toast.makeText(
                            this@CarDetailsActivity, "Deleted All Expenses", Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@CarDetailsActivity, "Deleted All Expenses", Toast.LENGTH_SHORT
                        )
                    }
                }
            }
            queryCar.get().addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val documentSnapshot: DocumentSnapshot = queryDocumentSnapshots.documents[0]
                    val productRef = documentSnapshot.reference

                    // Delete the product data
                    productRef.delete().addOnSuccessListener { aVoid: Void? ->
                        binding.progressBar.visibility = View.GONE
                        startActivity(
                            Intent(
                                this@CarDetailsActivity, MainActivity::class.java
                            )
                        )
                        finish()
                        Toast.makeText(
                            this@CarDetailsActivity, "Car Deleted", Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e: Exception? ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@CarDetailsActivity, e?.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@CarDetailsActivity, "No Vehicle Found", Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CarDetailsActivity, e.message, Toast.LENGTH_SHORT).show()
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