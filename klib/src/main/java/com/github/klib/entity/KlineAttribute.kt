package com.github.klib.entity

import java.io.Serializable

/**
 * 自定义属性封装
 */
data class KlineAttribute(
    //总的
    var candleUpColor: Int = 0,
    var candleDownColor: Int = 0,
    var pointWidth: Float = 0f,
    var textSize: Float = 0f,
    var textColor: Int = 0,
    var lineWidth: Float = 0f,//细线
    var backgroundColor: Int = 0,
    var selectedLineColor: Int = 0,//选中某列，高亮线颜色
    var selectedLineWidth: Float = 0f,
    var gridLineWidth: Float = 0f,
    var gridLineColor: Int = 0,
    var gridRows: Int = 5,//网格行数
    var gridColumns: Int = 5,//网格列数
    //macd
    var macdWidth: Float = 0f,
    var difColor: Int = 0,
    var deaColor: Int = 0,
    var macdColor: Int = 0,
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
    //wr
    var wrColor: Int = 0,
    //main
    var ma5Color: Int = 0,
    var ma10Color: Int = 0,
    var ma30Color: Int = 0,
    var candleWidth: Float = 0f,//蜡烛宽度
    var candleLineWidth: Float = 0f,
    var selectorBackgroundColor: Int = 0,
    var selectorTextSize: Float = 0f,
    var candleSolid: Boolean = true,//蜡烛是否实心,目前仅实心
    //分时线
    var timeLineWidth: Float = 0f, //分时线颜色
    var timeLineColor: Int = 0, //分时线颜色
    var timeLineShaderColorTop: Int = 0, //分时线颜色
    var timeLineShaderColorBtm: Int = 0 //分时线颜色
) : Serializable