package com.sokoldev.mygarage.report

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sokoldev.mygarage.vehicle.data.Car

class ReportAdapter(context: Context, persons: List<Car>) :
    ArrayAdapter<String>(
        context,
        android.R.layout.simple_spinner_item,
        persons.map { it.make + " " + it.model }) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (view is TextView) {
            // Set the text color of the selected item
            view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        if (view is TextView) {
            // Set the text color of the drop-down items
            view.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        return view
    }
}
