package com.github.klib.entity

/**
 * 深度
 */
open class DepthEntity : Comparable<DepthEntity> {
    override fun compareTo(other: DepthEntity): Int {
        return when {
            price > other.price -> 1
            price < other.price -> -1
            else -> 0
        }
    }

    constructor() {

    }

    constructor(price: Double, amount: Double) {
        this.price = price
        this.amount = amount
    }

    var price: Double = 0.0
    var amount: Double = 0.0
    var x: Float = 0f
    var y: Float = 0f
}