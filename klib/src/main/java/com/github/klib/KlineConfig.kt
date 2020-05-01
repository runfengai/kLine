package com.github.klib

/**
 * 参数配置信息
 */
object KlineConfig {
    const val TYPE_NULL_SUB = -1//无副图
    //是否有副图
    const val TYPE_SUB_MACD = 1
    const val TYPE_SUB_KDJ = 2
    const val TYPE_SUB_RSI = 3
    const val TYPE_SUB_WR = 4
    //主图类型
    const val TYPE_MAIN_CANDLE = 1
    const val TYPE_MAIN_TIME_LINE = 2
}