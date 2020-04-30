package com.github.klib.entity

import java.util.*

/**
 * 对外暴露
 */
open class KEntity(
    var dateTime: String = "",
    /**
     * BOLL线上轨、中轨、下轨
     */
    var up: Float = 0f,
    var mb: Float = 0f,
    var dn: Float = 0f,
    /**
     * 蜡烛线
     */
    var change: Float = 0f,//涨跌额
    var changePercent: String = "",//涨跌幅
    var open: Float = 0f,//开盘价
    var highest: Float = 0f,//最高价
    var lowest: Float = 0f,//最低价
    var close: Float = 0f,//收盘价
    var MA5: Float = 0f,//五日（或月、时、分）线均价
    var MA10: Float = 0f,//十日（或月、时、分）线均价
    var MA30: Float = 0f,//三十日（或月、时、分）线均价
    /**
     * KDJ指标
     */
    var k: Float = 0f,//k值
    var d: Float = 0f,//D值
    var j: Float = 0f,//D值
    /**
     * MACD指标
     */
    var dea: Float = 0f,//DEA值
    var dif: Float = 0f,//DIF值
    var macd: Float = 0f,//MACD值

    /**
     * 分时线
     */
    var avgPrice: Float = 0f,//均价
    var price: Float = 0f,//成交价
    var date: Date = Date(),//时间
    var volume: Float = 0f,//成交量
    var ma5Volume: Float = 0f,//成交量
    var ma10Volume: Float = 0f,//成交量

    var rsi1: Float = 0f,
    var rsi2: Float = 0f,
    var rsi3: Float = 0f,//rsi指标

    var wr:Float=0f//

)