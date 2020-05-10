package com.github.klib.interfaces

import android.graphics.Canvas
import com.github.klib.BaseKChartView
import com.github.klib.entity.DefValueFormatter

/**
 *
 */
abstract class IChartDraw<T>(private val baseKChartView: BaseKChartView) {
    /**
     *滑动时绘制
     */
    abstract fun drawTranslated(
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
    abstract fun drawText(canvas: Canvas, position: Int, x: Float, y: Float)


    abstract fun getMaxValue(point: T): Float

    abstract fun getMinValue(point: T): Float

   open fun getValueFormatter(): IValueFormatter {
        return baseKChartView.mValueFormatter
    }

    abstract fun setAttr()

}