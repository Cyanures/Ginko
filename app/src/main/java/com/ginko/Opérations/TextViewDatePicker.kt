package com.ginko.Opérations

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class TextViewDatePicker constructor(
    private val mContext: Context,
    private val mView: TextView
) : View.OnClickListener, DatePickerDialog.OnDateSetListener {
    var datePickerDialog: DatePickerDialog? = null
        private set

    init {
        mView.setOnClickListener(this)
        mView.isFocusable = false
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val date = calendar.time

        val formatter = SimpleDateFormat(DATE_SERVER_PATTERN)
        mView.text = formatter.format(date)
    }

    override fun onClick(v: View) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        datePickerDialog = DatePickerDialog(
            mContext, this, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog!!.show()
    }

    companion object {
        val DATE_SERVER_PATTERN = "dd-MM-yyyy"
    }
}