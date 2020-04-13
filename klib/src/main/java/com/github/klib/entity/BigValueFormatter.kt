package com.github.klib.entity

import android.content.Context
import com.github.klib.R
import com.github.klib.interfaces.IValueFormatter
import java.util.*

/**
 *volumn用于显示总量的
 */
class BigValueFormatter : IValueFormatter {

    private var units: Array<String>

    constructor(context: Context) {
        units = context.resources.getStringArray(R.array.unit)
    }

    val values = arrayListOf(1000, 1000000, 1000000000)

    override fun format(value: Float): String {
        var unit = ""
        var i = values.size - 1
        while (i >= 0) {
            if (value > values[i]) {
                unit = units[i]
                break
            }
        }

        return String.format(Locale.getDefault(), "%.2f", value) + unit
    }

}