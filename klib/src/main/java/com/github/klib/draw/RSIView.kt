package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.max
import kotlin.math.min

class RSIView(private var baseKchartView: BaseKChartView) : IChartDraw<KEntity> {

    private val rsi1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rsi2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rsi3Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun setAttr() {
        baseKchartView.klineAttribute.apply {
            rsi1Paint.color = rsi1Color
            rsi2Paint.color = rsi2Color
            rsi3Paint.color = rsi3Color

            rsi1Paint.strokeWidth = lineWidth
            rsi2Paint.strokeWidth = lineWidth
            rsi3Paint.strokeWidth = lineWidth

            rsi1Paint.textSize = textSize
            rsi2Paint.textSize = textSize
            rsi3Paint.textSize = textSize
        }
    }

    override fun drawTranslated(
        lastPoint: KEntity,
        currPoint: KEntity,
        lastX: Float,
        currX: Float,
        canvas: Canvas,
        position: Int
    ) {
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.rsi1),
            currX,
            baseKchartView.getSubY(currPoint.rsi1),
            rsi1Paint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.rsi2),
            currX,
            baseKchartView.getSubY(currPoint.rsi2),
            rsi2Paint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.rsi3),
            currX,
            baseKchartView.getSubY(currPoint.rsi3),
            rsi3Paint
        )

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: KEntity): Float {
        return max(point.rsi1, max(point.rsi2, point.rsi3))
    }

    override fun getMinValue(point: KEntity): Float {
        return min(point.rsi1, min(point.rsi2, point.rsi3))
    }

    init {
        setAttr()
    }
}