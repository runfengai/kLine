package com.github.klib.draw

import android.graphics.Canvas
import com.github.klib.BaseKChartView
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.max
import kotlin.math.min

class RSIView(private var baseKchartView: BaseKChartView) : IChartDraw<KEntity> {
    override fun drawTranslated(
        lastPoint: KEntity,
        currPoint: KEntity,
        lastX: Float,
        currX: Float,
        canvas: Canvas,
        position: Int
    ) {

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: KEntity): Float {
        return max(point.rsi1, max(point.rsi2, point.rsi3))
    }

    override fun getMinValue(point: KEntity): Float {
        return min(point.rsi1, min(point.rsi2, point.rsi3))
    }


}