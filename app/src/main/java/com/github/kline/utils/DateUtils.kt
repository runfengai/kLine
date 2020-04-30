package com.github.kline.utils

import com.github.kline.DataParse.Companion.DATE_FORMAT_0


import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import kotlin.math.abs

/**
 * Created by Administrator on 2017/9/1.
 */

object DateUtils {

    private val dateLocal = ThreadLocal<SimpleDateFormat>()

    val dateFormat: SimpleDateFormat?
        get() {
            if (null == dateLocal.get()) {
                dateLocal.set(SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE))
            }
            return dateLocal.get()
        }

    /**
     * 将时间戳转化成固定格式（默认 yyyy-MM-dd HH:mm:ss 当前时间 ）
     */
    fun getFormatTime(dateLong: Long, format: String? = "yyyy-MM-dd HH:mm:ss"): String {
        val date = Date(dateLong)
        val sdf = SimpleDateFormat(format ?: DATE_FORMAT_0, Locale.SIMPLIFIED_CHINESE)
        return sdf.format(date)
    }

    fun getFormatTime(format: String = "yyyy-MM-dd HH:mm:ss", date: Date?): String {
        var dateTmp = date
        if (dateTmp == null) {
            dateTmp = Date()
        }
//        val sdf =SimpleDateFormat.getDateInstance().format(format,date)
        val sdf = SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE)
        return sdf.format(dateTmp)
    }

    /**
     * 将固定格式转化成时间戳（默认 yyyy-MM-dd HH:mm:ss）
     */
    fun getTimeMillis(format: String = "yyyy-MM-dd HH:mm:ss", dateString: String): Long {
        val sdf = SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE)
        try {
            val date = sdf.parse(dateString)
            return date!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 将固定格式转化成时间戳( HH:mm:ss）
     */
    fun getTimeMillisFromHourMinuteSecond(format: String, dateString: String): Long {
        val sdf = SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE)
        try {
            val date = sdf.parse(dateString)
            val calendarNow = Calendar.getInstance()
            val calendarTarget = Calendar.getInstance()
            calendarTarget.time = date!!
            calendarTarget.set(Calendar.YEAR, calendarNow.get(Calendar.YEAR))
            calendarTarget.set(Calendar.MONTH, calendarNow.get(Calendar.MONTH))
            calendarTarget.set(Calendar.DAY_OF_YEAR, calendarNow.get(Calendar.DAY_OF_YEAR))
            return calendarTarget.time.time
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 将时间戳转date
     */
    fun getDate(pattern: String = "HH:mm", dateString: Long?): Date {
        val format = SimpleDateFormat(pattern, Locale.SIMPLIFIED_CHINESE)
        val d = format.format(dateString)
        val date: Date?
        try {
            date = format.parse(d)
            return date
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return Date()
    }
    const val KLINE_DATE_FORMAT="MM-dd HH:mm"
    /**
     * 比较两个时间
     *
     * @return
     */
    fun compareDate(startTime: String, endTime: String): Long {
        val simpleDateFormat = SimpleDateFormat(KLINE_DATE_FORMAT, Locale.SIMPLIFIED_CHINESE)
        try {
            val start = simpleDateFormat.parse(startTime)
            val end = simpleDateFormat.parse(endTime)
            val res = Math.abs((end!!.time - start!!.time) / (60 * 1000))
            return res
        } catch (e: ParseException) {

        }

        return 0

    }

    /**
     * 比较两个时间
     *
     * @return
     */
    fun compareMills(startTime: Long, endTime: Long): Long {
        var res = abs(endTime - startTime)
        val max = (60 * 1000).toLong()//1分钟
        if (res > max) {
            res = max
        }
        return res
    }

    /**
     * string转date
     *
     * @param strTime
     * @param formatType
     * @return
     */
    fun getDateTransformString(strTime: String, formatType: String = "HH:mm"): Date {
        val formatter = SimpleDateFormat(formatType, Locale.SIMPLIFIED_CHINESE)
        val date: Date?
        try {
            date = formatter.parse(strTime)
            return date
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return Date()
    }

    /**
     * 判断是否为今天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun IsToday(day: String): Boolean {

        val pre = Calendar.getInstance()
        val predate = Date(System.currentTimeMillis())
        pre.time = predate

        val cal = Calendar.getInstance()
        val date = dateFormat?.parse(day)
        cal.time = date!!

        if (cal.get(Calendar.YEAR) == pre.get(Calendar.YEAR)) {
            val diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR)

            if (diffDay == 0) {
                return true
            }
        }
        return false
    }

    /**
     * 判断是否为昨天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun IsYesterday(day: String): Boolean {

        val pre = Calendar.getInstance()
        val predate = Date(System.currentTimeMillis())
        pre.time = predate

        val cal = Calendar.getInstance()
        val date = dateFormat?.parse(day)
        cal.time = date!!

        if (cal.get(Calendar.YEAR) == pre.get(Calendar.YEAR)) {
            val diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR)

            if (diffDay == -1) {
                return true
            }
        }
        return false
    }

    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss
     *
     * @param millisecond
     * @return
     */
    fun getDateTimeFromMillisecond(millisecond: Long?): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)
        val date = Date(millisecond!!)
        return simpleDateFormat.format(date)
    }


    fun isSameWeek(currDate: Date, lastDate: Date): Boolean {
        // 0.先把Date类型的对象转换Calendar类型的对象
        val currCal = Calendar.getInstance()
        val lastCal = Calendar.getInstance()
        currCal.time = currDate
        lastCal.time = lastDate
        // 1.比较当前日期在年份中的周数是否相同
        return currCal.get(Calendar.WEEK_OF_YEAR) == lastCal.get(Calendar.WEEK_OF_YEAR)
    }

    fun isSameMonth(currDate: Date, lastDate: Date): Boolean {
        // 0.先把Date类型的对象转换Calendar类型的对象
        val currCal = Calendar.getInstance()
        val lastCal = Calendar.getInstance()
        currCal.time = currDate
        lastCal.time = lastDate
        // 1.比较当前日期在年份中的月是否相同
        return currCal.get(Calendar.MONTH) == lastCal.get(Calendar.MONTH)
    }

}
