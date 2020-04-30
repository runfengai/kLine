package com.github.kline.entity


import com.github.klib.entity.KEntity
import java.util.*

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 数据辅助类 计算macd rsi等
 */

object DataHelper {
    fun getALL(list: List<KEntity>?): List<KEntity> {
        if (list != null) {
            calculate(list)
            return list
        }
        return ArrayList()
    }

    /**
     * 计算RSI
     *
     * @param list
     */
    private fun calculateRSI(list: List<KEntity>) {
        var rsi1: Float
        var rsi2: Float
        var rsi3: Float
        var rsi1ABSEma = 0f
        var rsi2ABSEma = 0f
        var rsi3ABSEma = 0f
        var rsi1MaxEma = 0f
        var rsi2MaxEma = 0f
        var rsi3MaxEma = 0f
        for (i in list.indices) {
            val point = list[i]
            val closePrice = point.close
            if (i == 0) {
                rsi1 = 0f
                rsi2 = 0f
                rsi3 = 0f
                rsi1ABSEma = 0f
                rsi2ABSEma = 0f
                rsi3ABSEma = 0f
                rsi1MaxEma = 0f
                rsi2MaxEma = 0f
                rsi3MaxEma = 0f
            } else {
                val rMax = max(0f, closePrice - list[i - 1].close)
                val rAbs = abs(closePrice - list[i - 1].close)
                rsi1MaxEma = (rMax + (6f - 1) * rsi1MaxEma) / 6f
                rsi1ABSEma = (rAbs + (6f - 1) * rsi1ABSEma) / 6f

                rsi2MaxEma = (rMax + (12f - 1) * rsi2MaxEma) / 12f
                rsi2ABSEma = (rAbs + (12f - 1) * rsi2ABSEma) / 12f

                rsi3MaxEma = (rMax + (24f - 1) * rsi3MaxEma) / 24f
                rsi3ABSEma = (rAbs + (24f - 1) * rsi3ABSEma) / 24f

                rsi1 = rsi1MaxEma / rsi1ABSEma * 100
                rsi2 = rsi2MaxEma / rsi2ABSEma * 100
                rsi3 = rsi3MaxEma / rsi3ABSEma * 100
            }
            point.rsi1 = rsi1
            point.rsi2 = rsi2
            point.rsi3 = rsi3
        }
    }

    /**
     * 计算kdj
     *
     * @param list
     */
    private fun calculateKDJ(list: List<KEntity>) {
        var k = 0f
        var d = 0f

        for (i in list.indices) {
            val point = list[i]
            val closePrice = point.close
            var startIndex = i - 8
            if (startIndex < 0) {
                startIndex = 0
            }
            var max9 = java.lang.Float.MIN_VALUE
            var min9 = java.lang.Float.MAX_VALUE
            for (index in startIndex..i) {
                max9 = max(max9, list[index].highest)
                min9 = min(min9, list[index].lowest)

            }
            val rsv = 100f * (closePrice - min9) / (max9 - min9)
            if (i == 0) {
                k = rsv
                d = rsv
            } else {
                k = (rsv + 2f * k) / 3f
                d = (k + 2f * d) / 3f
            }
            point.k = k
            point.d = d
            point.j = 3f * k - 2 * d
        }

    }

    /**
     * 计算macd
     *
     * @param list
     */
    private fun calculateMACD(list: List<KEntity>) {
        var ema12 = 0f
        var ema26 = 0f
        var dif: Float
        var dea = 0f
        var macd: Float

        for (i in list.indices) {
            val point = list[i]
            val closePrice = point.close
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else {
                //                EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                //                EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f
            }
            //            DIF = EMA（12） - EMA（26） 。
            //            今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            //            用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26
            dea = dea * 8f / 10f + dif * 2f / 10f
            macd = (dif - dea) * 2f
            point.dif = dif
            point.dea = dea
            point.macd = macd
        }

    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     *
     * @param list
     */
    private fun calculateBOLL(list: List<KEntity>) {
        for (i in list.indices) {
            val point = list[i]
            val closePrice = point.close
            if (i == 0) {
                point.mb = closePrice
                point.up = java.lang.Float.NaN
                point.dn = java.lang.Float.NaN
            } else {
                var n = 20
                if (i < 20) {
                    n = i + 1
                }
                var md = 0f
                for (j in i - n + 1..i) {
                    val c = list[j].close
                    val m = point.MA30
                    val value = c - m
                    md += value * value
                }
                md /= (n - 1)
                md = sqrt(md.toDouble()).toFloat()
                point.mb = point.MA30
                point.up = point.mb + 2f * md
                point.dn = point.mb - 2f * md
            }
        }

    }

    /**
     * 计算ma
     *
     * @param list
     */
    private fun calculateMA(list: List<KEntity>) {
        var ma5 = 0f
        var ma10 = 0f
        var ma30 = 0f

        for (i in list.indices) {
            val point = list[i]
            val closePrice = point.close

            ma5 += closePrice
            ma10 += closePrice
            ma30 += closePrice
            if (i >= 5) {
                ma5 -= list[i - 5].close
                point.MA5 = ma5 / 5f
            } else {
                point.MA5 = ma5 / (i + 1f)
            }
            if (i >= 10) {
                ma10 -= list[i - 10].close
                point.MA10 = ma10 / 10f
            } else {
                point.MA10 = ma10 / (i + 1f)
            }
            if (i >= 30) {
                ma30 -= list[i - 30].close
                point.MA30 = ma30 / 30f
            } else {
                point.MA30 = ma30 / (i + 1f)
            }
            point.change = point.close - point.open
            val p = point.change / point.open
            point.changePercent = String.format(Locale.getDefault(), "%.2f", p) + "%"
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param list
     */
    private fun calculate(list: List<KEntity>) {
        calculateMA(list)
        calculateMACD(list)
        calculateBOLL(list)
        calculateRSI(list)
        calculateKDJ(list)
        calculateVolumeMA(list)
        calculateWR(list)
    }

    private fun calculateWR(list: List<KEntity>) {
        val len = list.size
        var h14Max: Float
        var l14Min: Float

        for (i in 0 until len) {
            val kEntity = list[i]
            if (i >= 13) {
                h14Max = Float.MIN_VALUE
                l14Min = Float.MAX_VALUE
                for (j in i - 13..i) {
                    val jKEntity = list[j]
                    h14Max = max(h14Max, jKEntity.highest)
                    l14Min = min(l14Min, jKEntity.lowest)
                }
                kEntity.wr = (h14Max - kEntity.close) / (h14Max - l14Min) * 100
                println(kEntity.wr)
            }
        }

    }

    private fun calculateVolumeMA(entries: List<KEntity>) {
        var volumeMa5 = 0f
        var volumeMa10 = 0f

        for (i in entries.indices) {
            val entry = entries[i]

            volumeMa5 += entry.volume
            volumeMa10 += entry.volume

            if (i >= 5) {

                volumeMa5 -= entries[i - 5].volume
                entry.ma5Volume = volumeMa5 / 5f
            } else {
                entry.ma5Volume = volumeMa5 / (i + 1f)
            }

            if (i >= 10) {
                volumeMa10 -= entries[i - 10].volume
                entry.ma10Volume = volumeMa10 / 10f
            } else {
                entry.ma10Volume = volumeMa10 / (i + 1f)
            }
        }
    }
}
