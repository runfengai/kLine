package com.github.kline

import android.util.SparseArray
import com.github.klib.entity.KEntity


import org.json.JSONArray

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Administrator on 2018/2/10.
 */

class DataParse : Serializable {
    companion object {
        const val PATTERN_MIN = "MM-dd HH:mm"
        const val PATTERN_MIN_NEW = "HH:mm MM/dd"
        const val PATTERN_MIN_NEW_2 = "HH:mm:ss MM/dd/yyyy"
        const val PATTERN_MIN_NEW_3 = "yyyy/dd/MM HH:mm:ss"
        const val PATTERN_HOUR = "MM-dd HH:00"
        const val PATTERN_DAY = "yyyy-MM-dd"
        const val DATE_FORMAT_0 = "yyyy-MM-dd HH:mm:ss"

        //是否能被整除
        fun canBeDiv(currDate: Long, num: Long): Boolean {
            val remainder = currDate % num
            return remainder == 0L
        }

        fun getPatternByType(tag: Int): String {
            var pattern = PATTERN_MIN
            if (tag == GlobalConstant.TAG_AN_HOUR) {
                pattern = PATTERN_HOUR
            } else if (tag == GlobalConstant.TAG_DAY || tag == GlobalConstant.TAG_WEEK || tag == GlobalConstant.TAG_MONTH) {
                pattern = PATTERN_DAY
            }
            return pattern
        }


    }

    var kLineDatas = ArrayList<KEntity>()
        private set


    var volmax: Float = 0.toFloat()

    private var xValuesLabel = SparseArray<String>()


    /**
     * 将返回的k线数据转化成 自定义实体类
     */
    fun parseKLine(array: JSONArray, tag: Int) {
        //Entry高低 开收           //开高低收
        val len = array.length()
        if (len == 0) return

        val kLineMap = LinkedHashMap<String, MutableList<KEntity>>()
        //第一个日期
        var lastDateStr = "0"
        var firstGotTag = false//寻找满足的第一个数,如5分钟线，则必须找末尾分钟为0、5的

        for (i in 0 until len) {//k线
            val data = array.optJSONArray(i)
            val pattern = getPatternByType(tag)

            val dateMill = data.optLong(0)
            val dateStr = WonderfulDateUtils.getFormatTime(pattern, Date(dateMill))

            val sdf = SimpleDateFormat(pattern, Locale.SIMPLIFIED_CHINESE)
            if (!firstGotTag) {
                lastDateStr = dateStr
            }
            val currDate = sdf.parse(dateStr) ?: return
            val lastDate = sdf.parse(lastDateStr) ?: return
            //K线实体类
            val kLineData = KEntity()
            kLineData.dateTime = dateStr
            kLineData.open = data.optDouble(1).toFloat()
            kLineData.close = data.optDouble(4).toFloat()
            kLineData.highest = data.optDouble(2).toFloat()
            kLineData.lowest = data.optDouble(3).toFloat()
            kLineData.volume = data.optDouble(5).toFloat()


            var addNoMerge = true//添加、合并//不包含，则

            //先进去第一个,之后的每5分钟加进去
            if (tag == GlobalConstant.TAG_FIVE_MINUTE) {
                if (!firstGotTag) {
                    if (canBeDiv(currDate.time, 5 * 60 * 1000)) {
                        firstGotTag = true
                    } else {
                        continue
                    }
                }
                val min5 = (currDate.time - lastDate.time) / (60 * 1000)
                addNoMerge = min5 >= 5
            } else if (tag == GlobalConstant.TAG_FIFTEEN_MINUTE) {
                if (!firstGotTag) {
                    if (canBeDiv(currDate.time, 15 * 60 * 1000)) {
                        firstGotTag = true
                    } else {
                        continue
                    }
                }
                val min15 = (currDate.time - lastDate.time) / (60 * 1000)
                addNoMerge = min15 >= 15
            } else if (tag == GlobalConstant.TAG_THIRTY_MINUTE) {
                if (!firstGotTag) {
                    if (canBeDiv(currDate.time, 30 * 60 * 1000)) {
                        firstGotTag = true
                    } else {
                        continue
                    }
                }
                val min30 = (currDate.time - lastDate.time) / (60 * 1000)
                addNoMerge = min30 >= 30
            } else if (tag == GlobalConstant.TAG_WEEK) {
                if (!firstGotTag) {
                    firstGotTag = true
                }
//                val week = (currDate.time - lastDate.time) / (24 * 60 * 60 * 1000)
//                addNoMerge = week >= 7
                addNoMerge = !WonderfulDateUtils.isSameWeek(currDate, lastDate)
            } else if (tag == GlobalConstant.TAG_MONTH) {
                if (!firstGotTag) {
                    firstGotTag = true
                }
//                val mon = (currDate.time - lastDate.time) / (24 * 60 * 60 * 1000)
//                addNoMerge = mon >= 30
                addNoMerge = !WonderfulDateUtils.isSameMonth(currDate, lastDate)
            }

            val mapSavedList = kLineMap[lastDateStr]
            if (addNoMerge || mapSavedList == null) {
                //先进去第一个,之后的每5分钟加进去
                val list = mutableListOf<KEntity>()
                list.add(kLineData)
                kLineData.date = Date(dateMill)
                kLineMap[dateStr] = list
                //数据中间有可能会断，所以此处加上修正
                lastDateStr = getModifiedDate(pattern, dateStr, tag)
            } else {//需要合并
                mapSavedList.add(kLineData)
                kLineMap[lastDateStr] = mapSavedList
            }

        }

        var j = 0
        kLineMap.forEach {
            val key = it.key
            val list = it.value
            val kLineData = getMergeData(key, list)
            kLineDatas.add(kLineData)
            volmax = max(kLineData.volume, volmax)
            xValuesLabel.put(j++, kLineData.dateTime)
        }
        //
    }

    //获取修正后的日期
    private fun getModifiedDate(pattern: String, dateStr: String, tag: Int): String {
        val time = WonderfulDateUtils.getTimeMillis(pattern, dateStr)

        val reminder = when (tag) {
            GlobalConstant.TAG_FIVE_MINUTE -> time % (5 * 60 * 1000)
            GlobalConstant.TAG_FIFTEEN_MINUTE -> time % (15 * 60 * 1000)
            GlobalConstant.TAG_THIRTY_MINUTE -> time % (30 * 60 * 1000)
//            GlobalConstant.TAG_WEEK -> time % (7 * 24 * 60 * 60 * 1000)
//            GlobalConstant.TAG_MONTH -> time % (30 * 24 * 60 * 60 * 1000L)
            else -> return dateStr
        }
        return if (reminder != 0L) {
            val res = time - reminder
            WonderfulDateUtils.getFormatTime(pattern, Date(res))
        } else {
            dateStr
        }
    }


    private fun getMergeData(key: String, list: MutableList<KEntity>): KEntity {
        val res = list[0]
        res.dateTime = key
        for (index in 1 until list.size) {
            //处理数据
            val lastEntity = list[index]
            res.close = lastEntity.close
            res.volume += lastEntity.volume
            res.lowest = min(res.lowest, lastEntity.lowest)
            res.highest = max(res.highest, lastEntity.highest)
        }
        return res
    }


}
