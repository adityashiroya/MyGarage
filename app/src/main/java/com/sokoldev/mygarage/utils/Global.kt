package com.sokoldev.mygarage.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.*

object Global {

    val months = arrayOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    fun setupDatePicker(context :Context, editText: EditText) {
        val calendar = Calendar.getInstance()
        // Create a DatePickerDialog with the current date and set a listener to update the EditText
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Create a SimpleDateFormat object with the desired date format
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Set the selected date to the EditText
                calendar.set(year, month, dayOfMonth)
                val selectedDate = dateFormat.format(calendar.time)
                editText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }
}