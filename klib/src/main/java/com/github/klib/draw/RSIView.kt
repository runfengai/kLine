package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import kotlin.math.max
import kotlin.math.min

class RSIView(private var baseKChartView: BaseKChartView) :
    IChartDraw<KEntity>(baseKChartView) {

    private val rsi1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rsi2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rsi3Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
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
            baseKChartView.getSubY(lastPoint.rsi1),
            currX,
            baseKChartView.getSubY(currPoint.rsi1),
            rsi1Paint
        )
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.rsi2),
            currX,
            baseKChartView.getSubY(currPoint.rsi2),
            rsi2Paint
        )
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.rsi3),
            currX,
            baseKChartView.getSubY(currPoint.rsi3),
            rsi3Paint
        )
    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        val item = baseKChartView.getItem(position) ?: return
        var x0 = x
        var text = "RSI(1):${getValueFormatter().format(item.rsi1)}   "
        canvas.drawText(text, x0, y, rsi1Paint)
        x0 += rsi1Paint.measureText(text)
        text = "RSI(2):${getValueFormatter().format(item.rsi2)}   "
        canvas.drawText(text, x0, y, rsi2Paint)
        x0 += rsi2Paint.measureText(text)
        text = "RSI(3):${getValueFormatter().format(item.rsi3)}"
        canvas.drawText(text, x0, y, rsi3Paint)
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