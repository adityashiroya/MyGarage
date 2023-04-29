package com.sokoldev.mygarage.report

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sokoldev.mygarage.R
import com.sokoldev.mygarage.databinding.FragmentReportBinding
import com.sokoldev.mygarage.vehicle.data.Car
import com.sokoldev.mygarage.vehicle.data.MonthlyExpense


class ReportFragment : Fragment() {
    val carList: ArrayList<Car> = ArrayList()
    var expenseList: ArrayList<MonthlyExpense> = ArrayList()
    private lateinit var binding: FragmentReportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        getCarList()

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedVehicle = carList[position]
                val selectedVehicleId = selectedVehicle.id

                getVehicleExpense(selectedVehicleId)

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun getVehicleExpense(id: String?) {
        var db = FirebaseFirestore.getInstance()
        var productsRef = db.collection("monthlyExpenses")

        val query: Query = productsRef.whereEqualTo("carId", id)
        query.addSnapshotListener(EventListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@EventListener
            }

            expenseList.clear()
            for (document in snapshot!!.documents) {
                var expense = document.toObject(MonthlyExpense::class.java)
                if (expense != null) {
                    expenseList.add(expense)
                }
            }
            Log.d(
                "TAG",
                "Expense list: $expenseList"
            ) // Log expense list before passing to populateChart function
            activity?.runOnUiThread {
                populateChart(expenseList)
            }
        })
    }
    private fun populateChart(expenseList: ArrayList<MonthlyExpense>) {
        val barChart = binding.barChart

        val barDataSet = BarDataSet(getData(expenseList), "Monthly Expenses")
        barDataSet.color = resources.getColor(R.color.purple_500)

        val data = BarData(barDataSet)
        data.setValueTextSize(12f)

        barChart.data = data
        barChart.description.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.valueFormatter = IndexAxisValueFormatter(getLabels(expenseList))
        xAxis.labelCount = expenseList.size

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false

        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun getData(expenseList: ArrayList<MonthlyExpense>): ArrayList<BarEntry> {
        val dataEntries = ArrayList<BarEntry>()

        for ((index, expense) in expenseList.withIndex()) {
            expense.cost?.toFloat()?.let { BarEntry(index.toFloat(), it) }
                ?.let { dataEntries.add(it) }
        }

        return dataEntries
    }

    private fun getLabels(expenseList: ArrayList<MonthlyExpense>): ArrayList<String> {
        val labels = ArrayList<String>()

        for (expense in expenseList) {
            expense.month?.let { labels.add(it) }
        }

        return labels
    }



    private fun getCarList() {

        var db = FirebaseFirestore.getInstance()
        var productsRef = db.collection("cars")


        val query: Query =
            productsRef.whereEqualTo(
                "owner",
                FirebaseAuth.getInstance().currentUser?.uid.toString()
            )
        query.addSnapshotListener(EventListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@EventListener
            }

            for (document in snapshot!!.documents) {
                val car = document.toObject(Car::class.java)
                if (car != null) {
                    carList.add(car)
                }
            }
            showData(carList)
        })
    }

    private fun showData(carList: ArrayList<Car>) {
        val adapter = context?.let { ReportAdapter(it, carList) }
        binding.spinner.adapter = adapter
    }

}