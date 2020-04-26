package com.github.klib.interfaces

import android.graphics.Canvas
import com.github.klib.entity.DefValueFormatter

/**
 *
 */
interface IChartDraw<T> {
    /**
     *滑动时绘制
     */
    fun drawTranslated(
        lastPoint: T,
        currPoint: T,
        lastX: Float,
        currX: Float,
        canvas: Canvas,
        position: Int
    )

    /**
     * 绘制文字
     */
    fun drawText(canvas: Canvas, position: Int, x: Float, y: Float)


    fun getMaxValue(point: T): Float

    fun getMinValue(point: T): Float

    fun getValueFormatter(): IValueFormatter{
        return DefValueFormatter()
    }

}