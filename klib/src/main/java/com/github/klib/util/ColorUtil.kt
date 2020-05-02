package com.github.klib.util

import kotlin.math.max
import kotlin.math.min

object ColorUtil {
    /**
     * 给color添加透明度
     * @param alpha 透明度 0f～1f
     * @param baseColor 基本颜色
     * @return
     */
    fun getColorWithAlpha(alpha: Float, baseColor: Int): Int {
        val a = min(255, max(0, (alpha * 255).toInt())) shl 24
        val rgb = 0x00ffffff and baseColor
        return a + rgb
    }
}