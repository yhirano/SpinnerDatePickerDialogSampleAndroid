package com.github.yhirano.spinnerdatepickerdialog.compat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.DatePicker
import java.lang.reflect.Field

class DatePickerDialog : android.app.DatePickerDialog {
    constructor(
        context: Context,
        callback: OnDateSetListener?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) : super(context, callback, year, monthOfYear, dayOfMonth) {
        fixSpinner(context, year, monthOfYear, dayOfMonth)
    }

    constructor(
        context: Context,
        theme: Int,
        callback: OnDateSetListener?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) : super(context, theme, callback, year, monthOfYear, dayOfMonth) {
        fixSpinner(context, year, monthOfYear, dayOfMonth)
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun fixSpinner(context: Context, year: Int, month: Int, dayOfMonth: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            try {
                // Get the theme's android:datePickerMode
                val modeSpinner = 2
                val styleableClass = Class.forName("com.android.internal.R\$styleable")
                val datePickerStyleableField = styleableClass.getField("DatePicker")
                val datePickerStyleable = datePickerStyleableField[null] as IntArray

                val a = context.obtainStyledAttributes(
                    null, datePickerStyleable, android.R.attr.datePickerStyle, 0
                )
                val datePickerModeStyleableField =
                    styleableClass.getField("DatePicker_datePickerMode")
                val datePickerModeStyleable = datePickerModeStyleableField.getInt(null)
                val mode = a.getInt(datePickerModeStyleable, modeSpinner)
                a.recycle()

                if (mode == modeSpinner) {
                    val datePicker = findField(
                        android.app.DatePickerDialog::class.java,
                        DatePicker::class.java,
                        "mDatePicker"
                    )!!.get(this) as DatePicker
                    val delegateClass = Class.forName("android.widget.DatePickerSpinnerDelegate")
                    val delegateField = findField(
                        DatePicker::class.java, delegateClass, "mDelegate"
                    )!!
                    var delegate = delegateField[datePicker]
                    val spinnerDelegateClass =
                        Class.forName("android.widget.DatePickerSpinnerDelegate")

                    // In 7.0 Nougat for some reason the datePickerMode is ignored and the delegate is
                    // DatePickerClockDelegate
                    if (delegate.javaClass != spinnerDelegateClass) {
                        delegateField[datePicker] = null // throw out the DatePickerClockDelegate
                        datePicker.removeAllViews() // remove the DatePickerClockDelegate views
                        val createSpinnerUIDelegate = DatePicker::class.java.getDeclaredMethod(
                            "createSpinnerUIDelegate",
                            Context::class.java,
                            AttributeSet::class.java,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType
                        )
                        createSpinnerUIDelegate.isAccessible = true

                        // Instantiate a DatePickerSpinnerDelegate throughout createSpinnerUIDelegate method
                        delegate = createSpinnerUIDelegate.invoke(
                            datePicker,
                            context,
                            null,
                            android.R.attr.datePickerStyle,
                            0
                        )
                        delegateField[datePicker] = delegate // set the DatePicker.mDelegate to the spinner delegate
                        datePicker.calendarViewShown = false
                        // Initialize the date for the DatePicker delegate again
                        datePicker.init(year, month, dayOfMonth, this)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        private fun findField(
            objectClass: Class<*>,
            fieldClass: Class<*>,
            expectedName: String
        ): Field? {
            try {
                val field = objectClass.getDeclaredField(expectedName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                // nop
            }
            // search for it if it wasn't found under the expected ivar name
            for (searchField in objectClass.declaredFields) {
                if (searchField.type == fieldClass) {
                    searchField.isAccessible = true
                    return searchField
                }
            }
            return null
        }
    }
}