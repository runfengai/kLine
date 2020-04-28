package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.max
import kotlin.math.min

class KDJView(private var baseKchartView: BaseKChartView) : IChartDraw<KEntity> {
    private val kPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val jPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun setAttr() {
        baseKchartView.klineAttribute.apply {
            kPaint.color = kColor
            dPaint.color = dColor
            jPaint.color = jColor

            kPaint.strokeWidth = lineWidth
            dPaint.strokeWidth = lineWidth
            jPaint.strokeWidth = lineWidth

            kPaint.textSize = textSize
            dPaint.textSize = textSize
            jPaint.textSize = textSize
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
            baseKchartView.getSubY(lastPoint.k),
            currX,
            baseKchartView.getSubY(currPoint.k),
            kPaint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.d),
            currX,
            baseKchartView.getSubY(currPoint.d),
            dPaint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.j),
            currX,
            baseKchartView.getSubY(currPoint.j),
            jPaint
        )

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: KEntity): Float {
        return max(point.k, max(point.d, point.j))
    }

    override fun getMinValue(point: KEntity): Float {
        return min(point.k, min(point.d, point.j))
    }

    init {
        setAttr()
    }

}