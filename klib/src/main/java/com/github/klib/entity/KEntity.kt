package com.github.klib.entity

import java.util.*

/**
 * 对外暴露
 */
data class KEntity(
    /**
     * BOLL线上轨、中轨、下轨
     */
    var up: Float,
    var mb: Float,
    var dn: Float,
    /**
     * 蜡烛线
     */

    var open: Float,//开盘价
    var highest: Float,//最高价
    var lowest: Float,//最低价
    var close: Float,//收盘价
    var MA5: Float,//五日（或月、时、分）线均价
    var MA10: Float,//十日（或月、时、分）线均价
    var MA30: Float,//三十日（或月、时、分）线均价
    /**
     * KDJ指标
     */
    var k: Float,//k值
    var d: Float,//D值
    var j: Float,//D值
    /**
     * MACD指标
     */
    var dea: Float,//DEA值
    var dif: Float,//DIF值
    var macd: Float,//MACD值

    /**
     * 分时线
     */
    var avgPrice: Float,//均价
    var price: Float,//成交价
    var date: Date,//时间
    var volume: Float//成交量

)