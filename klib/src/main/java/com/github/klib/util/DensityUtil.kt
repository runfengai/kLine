package com.github.klib.util

import android.content.Context

object DensityUtil {
    private var density = -1f
    private fun getDensity(context: Context): Float {
        val appContext = context.applicationContext
        if (density <= 0f) {
            density = appContext.resources.displayMetrics.density
        }
        return density
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        return (dpValue * getDensity(context) + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        return (pxValue / getDensity(context) + 0.5f).toInt()
    }
}