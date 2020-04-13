package com.github.klib.interfaces

import java.util.*

/**
 * 时间格式化
 */
interface IDateTimeFormatter {
    fun format(Date: Date): String
}