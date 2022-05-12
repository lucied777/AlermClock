package com.example.alermclock

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class AlertDialogFlagment : DialogFragment(){

    interface OnAlertListener{
        fun onPositiveClick()
    }

    private lateinit var listener :OnAlertListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAlertListener) listener = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            setMessage("時間になりました！")
            setPositiveButton("OK"){dialog,which ->
                listener.onPositiveClick()
            }
        }.create()
    }
}

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener{

    interface  OnDateSlectedListener{
        fun onSelectedClick(year: Int, month: Int, day: Int)
    }
    private lateinit var listener : OnDateSlectedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDateSlectedListener) listener = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(),this,year,month,day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        listener.onSelectedClick(p1,p2,p3)
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{

    interface OnTimeSelectedListener{
        fun onSelectedClick(hour: Int, minute: Int)
    }

    private lateinit var listener: OnTimeSelectedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnTimeSelectedListener) listener = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(requireContext(),this,hour,minute,true)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        listener.onSelectedClick(p1,p2)
    }
}