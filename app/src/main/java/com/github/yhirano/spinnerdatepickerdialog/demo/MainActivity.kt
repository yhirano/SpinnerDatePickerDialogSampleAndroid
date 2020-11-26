package com.github.yhirano.spinnerdatepickerdialog.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.github.yhirano.spinnerdatepickerdialog.R
import com.github.yhirano.spinnerdatepickerdialog.compat.DatePickerDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private val button by lazy {
        findViewById<AppCompatButton>(R.id.button)
    }

    private val textView by lazy {
        findViewById<AppCompatTextView>(R.id.text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
                this,
//                R.style.DatePickerDialog_Spinner,
                { _, year, month, dayOfMonth ->
                    textView.text = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.toString()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}