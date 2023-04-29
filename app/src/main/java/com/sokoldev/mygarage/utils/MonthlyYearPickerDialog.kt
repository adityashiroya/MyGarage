package com.sokoldev.mygarage.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class MonthYearPickerDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var mListener: OnDateSetListener? = null

    interface OnDateSetListener {
        fun onDateSet(year: Int, monthOfYear: Int)
    }

    fun setListener(listener: OnDateSetListener) {
        mListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(requireActivity(), this, year, month, 0)

        // Set the date picker mode to show only the month and year
        datePickerDialog.datePicker.apply {
            spinnersShown = true
            calendarViewShown = false
        }

        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        mListener?.onDateSet(year, monthOfYear+1)
    }
}
