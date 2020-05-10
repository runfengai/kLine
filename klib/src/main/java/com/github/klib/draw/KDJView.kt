package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import kotlin.math.max
import kotlin.math.min

class KDJView(private var baseKChartView: BaseKChartView) :
    IChartDraw<KEntity>(baseKChartView) {
    private val kPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val jPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
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
            baseKChartView.getSubY(lastPoint.k),
            currX,
            baseKChartView.getSubY(currPoint.k),
            kPaint
        )
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.d),
            currX,
            baseKChartView.getSubY(currPoint.d),
            dPaint
        )
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.j),
            currX,
            baseKChartView.getSubY(currPoint.j),
            jPaint
        )

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        val item = baseKChartView.getItem(position) ?: return
        var text = "K:${getValueFormatter().format(item.k)}   "
        var x0 = x
        canvas.drawText(text, x0, y, kPaint)
        x0 += kPaint.measureText(text)
        text = "D:${getValueFormatter().format(item.d)}   "
        canvas.drawText(text, x0, y, dPaint)
        x0 += dPaint.measureText(text)
        text = "J:${getValueFormatter().format(item.j)}"
        canvas.drawText(text, x0, y, jPaint)
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