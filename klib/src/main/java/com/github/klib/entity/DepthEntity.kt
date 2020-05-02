package com.github.klib.entity

/**
 * 深度
 */
class DepthEntity : Comparable<DepthEntity> {
    override fun compareTo(other: DepthEntity): Int {
        return when {
            volume > other.volume -> 1
            volume < other.volume -> -1
            else -> 0
        }
    }

    var price: Float = 0f
    var volume: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
}