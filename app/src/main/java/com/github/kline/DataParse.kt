package com.github.kline

import android.util.SparseArray

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry

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

        fun addNoMerge(currDate: Long, lastDate: Long, tag: Int): Boolean {
            return when (tag) {
                GlobalConstant.TAG_ONE_MINUTE -> {
                    val min = (currDate - lastDate) / (60 * 1000)
                    min >= 1
                }
                GlobalConstant.TAG_FIVE_MINUTE -> {
                    val min5 = (currDate - lastDate) / (60 * 1000)
                    min5 >= 5
                }
                GlobalConstant.TAG_FIFTEEN_MINUTE -> {
                    val min15 = (currDate - lastDate) / (60 * 1000)
                    min15 >= 15
                }
                GlobalConstant.TAG_THIRTY_MINUTE -> {
                    val min30 = (currDate - lastDate) / (60 * 1000)
                    min30 >= 30
                }
                GlobalConstant.TAG_AN_HOUR -> {
                    val hour = (currDate - lastDate) / (60 * 60 * 1000)
                    hour >= 1
                }
                GlobalConstant.TAG_DAY -> {
                    val week = (currDate - lastDate) / (24 * 60 * 60 * 1000)
                    week >= 1
                }
                GlobalConstant.TAG_WEEK -> {
                    val week = (currDate - lastDate) / (24 * 60 * 60 * 1000)
                    week >= 7
                }
                GlobalConstant.TAG_MONTH -> {
                    val mon = (currDate - lastDate) / (24 * 60 * 60 * 1000)
                    mon >= 30
                }
                else -> true
            }
        }

    }

    var timeDatas = ArrayList<MinutesBean>()
    var kLineDatas = ArrayList<KLineBean>()
        private set
    private var xVals = ArrayList<String>()//X轴数据
    var barEntries = ArrayList<BarEntry>()//成交量数据
    var candleEntries = ArrayList<CandleEntry>()//K线数据
    var minutesLine = ArrayList<Entry>()//分时数据

    var ma5DataL = ArrayList<Entry>()
    var ma10DataL = ArrayList<Entry>()
    var ma20DataL = ArrayList<Entry>()
    var ma30DataL = ArrayList<Entry>()

    var ma5DataV = ArrayList<Entry>()
    var ma10DataV = ArrayList<Entry>()
    var ma20DataV = ArrayList<Entry>()
    var ma30DataV = ArrayList<Entry>()

    var baseValue: Float = 0.toFloat()
    var permaxmin: Float = 0.toFloat()
    var volmax: Float = 0.toFloat()

    private var xValuesLabel = SparseArray<String>()

    /**
     * 得到Y轴最小值
     */
    val min: Float
        get() = baseValue - permaxmin

    /**
     * 得到Y轴最大值
     */
    val max: Float
        get() = baseValue + permaxmin

    /**
     * 得到百分百最大值
     *
     * @return
     */
    val percentMax: Float
        get() = permaxmin / baseValue

    /**
     * 得到百分比最小值
     *
     * @return
     */
    val percentMin: Float
        get() = -percentMax

    fun parseMinutes(`object`: JSONArray?) {
        if (`object` == null) return
        /*数据解析依照自己需求来定，如果服务器直接返回百分比数据，则不需要客户端进行计算*/
        val count = `object`.length()
        for (i in 0 until count) {
            val data = `object`.optJSONArray(i)
            //            String[] t = object.optString(i).split(" ");/*  "0930 9.50 4707",*/
            val minutesData = MinutesBean()
            minutesData.open = data.optDouble(1).toFloat()
            minutesData.close = data.optDouble(4).toFloat()
            if (i == 0) {
                this.baseValue = minutesData.close
            }
            minutesData.high = data.optDouble(2).toFloat()
            minutesData.low = data.optDouble(3).toFloat()

            minutesData.time = WonderfulDateUtils.getFormatTime("HH:mm", Date(data.optLong(0)))
            minutesData.cjprice = data.optDouble(4).toFloat()
            minutesData.cjnum = data.optDouble(5).toFloat()
            minutesData.total = minutesData.cjnum * minutesData.cjprice
            minutesData.avprice = minutesData.cjprice
            minutesData.cha = minutesData.cjprice - baseValue
            minutesData.per = minutesData.cha / baseValue
            val cha = (minutesData.cjprice - baseValue).toDouble()
            if (Math.abs(cha) > permaxmin) {
                permaxmin = Math.abs(cha).toFloat()
            }
            volmax = max(minutesData.cjnum, volmax)
            timeDatas.add(minutesData)
        }
        if (permaxmin == 0f) {
            permaxmin = baseValue * 0.02f
        }
    }


    /**
     * 将返回的k线数据转化成 自定义实体类
     */
    fun parseKLine(array: JSONArray) {
        //Entry高低 开收           //开高低收
//        val sdf = SimpleDateFormat("HH:mm")
        var i = 0
        val len = array.length()
        while (i < len) {
            val data = array.optJSONArray(i)
            val date = WonderfulDateUtils.getFormatTime("HH:mm", Date(data.optLong(0)))
            //K线实体类
            val kLineData = KLineBean(
                date,
                data.optDouble(1).toFloat(),
                data.optDouble(4).toFloat(),
                data.optDouble(2).toFloat(),
                data.optDouble(3).toFloat(),
                data.optDouble(5).toFloat()
            )
            kLineDatas.add(kLineData)
            volmax = Math.max(kLineData.vol, volmax)
            xValuesLabel.put(i, kLineData.date)
            minutesLine.add(Entry(data.optDouble(4).toFloat(), i.toFloat()))
            i++
        }
    }


    /**
     * 将返回的k线数据转化成 自定义实体类
     */
    fun parseKLine(array: JSONArray, tag: Int) {
        //Entry高低 开收           //开高低收
        val len = array.length()
        if (len == 0) return

        val kLineMap = LinkedHashMap<String, MutableList<KLineBean>>()
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
            val kLineData = KLineBean(
                dateStr,
                data.optDouble(1).toFloat(),
                data.optDouble(4).toFloat(),
                data.optDouble(2).toFloat(),
                data.optDouble(3).toFloat(),
                data.optDouble(5).toFloat()
            )
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
                val list = mutableListOf<KLineBean>()
                list.add(kLineData)
                kLineData.dateLong = dateMill
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
            volmax = max(kLineData.vol, volmax)
            xValuesLabel.put(j, kLineData.date)
            minutesLine.add(Entry(kLineData.close, j.toFloat()))
            j++
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


    private fun getMergeData(key: String, list: MutableList<KLineBean>): KLineBean {
        val res = list[0]
        res.date = key
        for (index in 1 until list.size) {
            //处理数据
            val lastEntity = list[index]
            res.close = lastEntity.close
            res.vol += lastEntity.vol
            res.low = min(res.low, lastEntity.low)
            res.high = max(res.high, lastEntity.high)
        }
        return res
    }


    /**
     * 构建Entry数据
     *
     * @param datas
     */
    fun initLineDatas(datas: ArrayList<KLineBean>?) {
        if (null == datas) return
        xVals = ArrayList()//X轴数据
        barEntries = ArrayList()//成交量数据
        candleEntries = ArrayList()//K线数据
        var i = 0
        var j = 0
        while (i < datas.size) {
            xVals.add(datas[i].date + "")
            val barfloats = floatArrayOf(
                datas[i].high,
                datas[i].low,
                datas[i].open,
                datas[i].close,
                datas[i].vol
            )
            barEntries.add(BarEntry(i.toFloat(), barfloats))
            candleEntries.add(
                CandleEntry(
                    i.toFloat(),
                    datas[i].high,
                    datas[i].low,
                    datas[i].open,
                    datas[i].close
                )
            )
            i++
            j++
        }
    }

    /**
     * 初始化K线图MA均线
     */
    fun initKLineMA(datas: ArrayList<KLineBean>?) {
        if (null == datas) {
            return
        }
        ma5DataL = ArrayList()
        ma10DataL = ArrayList()
        ma20DataL = ArrayList()
        ma30DataL = ArrayList()

        val kmaEntity5 = KMAEntity(datas, 5)
        val kmaEntity10 = KMAEntity(datas, 10)
        val kmaEntity20 = KMAEntity(datas, 20)
        val kmaEntity30 = KMAEntity(datas, 30)
        for (i in 0 until kmaEntity5.mAs.size) {
            if (i >= 5)
                ma5DataL.add(Entry(kmaEntity5.mAs[i], i.toFloat()))
            if (i >= 10)
                ma10DataL.add(Entry(kmaEntity10.mAs[i], i.toFloat()))
            if (i >= 20)
                ma20DataL.add(Entry(kmaEntity20.mAs[i], i.toFloat()))
            if (i >= 30)
                ma30DataL.add(Entry(kmaEntity30.mAs[i], i.toFloat()))
        }

    }

    /**
     * 初始化成交量MA均线
     */
    fun initVlumeMA(datas: ArrayList<KLineBean>?) {
        if (null == datas) {
            return
        }
        ma5DataV = ArrayList()
        ma10DataV = ArrayList()
        ma20DataV = ArrayList()
        ma30DataV = ArrayList()

        val vmaEntity5 = VMAEntity(datas, 5)
        val vmaEntity10 = VMAEntity(datas, 10)
        val vmaEntity20 = VMAEntity(datas, 20)
        val vmaEntity30 = VMAEntity(datas, 30)
        for (i in 0 until vmaEntity5.mAs.size) {
            ma5DataV.add(Entry(vmaEntity5.mAs[i], i.toFloat()))
            ma10DataV.add(Entry(vmaEntity10.mAs[i], i.toFloat()))
            ma20DataV.add(Entry(vmaEntity20.mAs[i], i.toFloat()))
            ma30DataV.add(Entry(vmaEntity30.mAs[i], i.toFloat()))
        }

    }

    fun setkDatas(kDatas: ArrayList<KLineBean>) {
        this.kLineDatas = kDatas
    }

    fun getxVals(): ArrayList<String> {
        return xVals
    }

    fun setxVals(xVals: ArrayList<String>) {
        this.xVals = xVals
    }

    fun getxValuesLabel(): SparseArray<String> {
        return xValuesLabel
    }

    fun setxValuesLabel(xValuesLabel: SparseArray<String>) {
        this.xValuesLabel = xValuesLabel
    }


    //根据一分钟线，或者日线，转换成对应线
    fun getKlineDataByType(
        kLineEntities: ArrayList<KLineEntity>,
        type: Int
    ): ArrayList<KLineEntity> {
        when (type) {
            GlobalConstant.TAG_ONE_MINUTE -> {
                return kLineEntities
            }
            GlobalConstant.TAG_FIVE_MINUTE -> {
                for (kLineEntity in kLineEntities) {
                    kLineEntity.datetime

                }

            }
            GlobalConstant.TAG_FIFTEEN_MINUTE -> {

            }
            GlobalConstant.TAG_THIRTY_MINUTE -> {

            }
            GlobalConstant.TAG_AN_HOUR -> {
                return kLineEntities
            }
            GlobalConstant.TAG_DAY -> {
                return kLineEntities
            }
            GlobalConstant.TAG_WEEK -> {

            }
            GlobalConstant.TAG_MONTH -> {

            }
        }
        return kLineEntities
    }


}
