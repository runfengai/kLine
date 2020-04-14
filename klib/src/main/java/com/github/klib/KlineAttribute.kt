package com.github.klib

/**
 * 自定义属性封装
 */
data class KlineAttribute(
    var pointWidth: Float = 0f,
    var textSize: Float = 0f,
    var textColor: Int = 0,
    var lineWidth: Float = 0f,
    var backgroundColor: Int = 0,
    var selectedLineColor: Int = 0,
    var selectedLineWidth: Float = 0f,
    var gridLineWidth: Float = 0f,
    var gridLineColor: Float = 0f,
    //macd
    var macdWidth: Float = 0f,
    var difColor: Float = 0f,
    var deaColor: Float = 0f,
    var macdColor: Float = 0f,
    //kdj
    var kColor: Int = 0,
    var dColor: Int = 0,
    var jColor: Int = 0,
    //rsi
    var rsi1Color: Int = 0,
    var rsi2Color: Int = 0,
    var rsi3Color: Int = 0,
    //boll
    var upColor: Int = 0,
    var mbColor: Int = 0,
    var dnColor: Int = 0,
    //main
    var ma5Color: Int = 0,
    var ma10Color: Int = 0,
    var ma30Color: Int = 0,
    var candleWidth: Float = 0f,//蜡烛宽度？？
    var candleLineWidth: Float = 0f,
    var selectorBackgroundColor: Int = 0,
    var selectorTextSize: Float = 0f,
    var candleSolid: Boolean = true//蜡烛是否空心
)