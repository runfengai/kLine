package com.github.kline.entity

data class ResponseEntity<out T>(var code: Int, val data: T?, var msg: String?="", var ts: Long?=0)
data class KData(var results: ArrayList<ArrayList<Double>>?)

//data class KLineEntity:KEntity(){
//
//}