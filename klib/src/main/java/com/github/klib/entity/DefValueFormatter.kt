package com.github.klib.entity

import com.github.klib.interfaces.IValueFormatter

class DefValueFormatter : IValueFormatter {
    private var scale: Int

    constructor(scale: Int = 2) {
        this.scale = scale
    }

    override fun format(value: Float): String {
        return String.format("%.${scale}f", value)
    }
}