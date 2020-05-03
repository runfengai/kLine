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

    var price: Float = 0f
    var amount: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
}