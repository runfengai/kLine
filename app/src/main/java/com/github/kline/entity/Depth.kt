package com.github.kline.entity

import com.github.klib.entity.DepthEntity

data class Book(
    val symbol: String = "",
    val data: BookItem,
    val type: String = "",
    val sequenceId: Long = 0
)


data class BookItem(
    val symbolId: Long = 0,
    val price: Double = 0.0,
    val sellOrders: List<List<Double>>?,
    val buyOrders: List<List<Double>>?,
    val sequenceId: Long = 0
)

class OrdersItem : DepthEntity {
    constructor()
    constructor(volume: Float, price: Float) {
        this.price = price
        this.amount = volume
    }

    constructor(volume: Float = 0f, price: Float = 0f, nullTag: Boolean = false) {
        this.price = price
        this.amount = volume
        this.nullTag = nullTag
    }

    var nullTag: Boolean = false//是否为占位的
}

//盘口深度快照信息
data class SocketSnapShot(
    val symbol: String = "",
    val price: Double = 0.0,
    val sellOrders: MutableList<OrdersItem>,
    val buyOrders: MutableList<OrdersItem>,
    val sequenceId: Long = 0,
    val timestamp: Long = 0
)
