package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import kotlin.math.max
import kotlin.math.min

class MACDView(private var baseKChartView: BaseKChartView) :
    IChartDraw<KEntity>(baseKChartView) {
    //macd
    private val downPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val upPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //指标
    private val difPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val deaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //文字
    private val macdPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius: Float = 0f
    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
            radius = lineWidth
            downPaint.color = candleDownColor
            upPaint.color = candleUpColor
            difPaint.color = difColor
            deaPaint.color = deaColor
            macdPaint.color = macdColor

            difPaint.strokeWidth = lineWidth
            deaPaint.strokeWidth = lineWidth
            macdPaint.strokeWidth = lineWidth

            difPaint.textSize = textSize
            deaPaint.textSize = textSize
            macdPaint.textSize = textSize
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
        drawMACD(currPoint, currX, canvas)
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.dif),
            currX,
            baseKChartView.getSubY(currPoint.dif),
            difPaint
        )
        canvas.drawLine(
            lastX,
            baseKChartView.getSubY(lastPoint.dea),
            currX,
            baseKChartView.getSubY(currPoint.dea),
            deaPaint
        )
    }


    private fun drawMACD(currPoint: KEntity, currX: Float, canvas: Canvas) {
        val macd = currPoint.macd

        val paint = if (macd >= 0) {
            upPaint
        } else {
            downPaint
        }
        canvas.drawRect(
            currX - radius,
            baseKChartView.getSubY(macd),
            currX + radius,
            baseKChartView.getSubY(0f),
            paint
        )

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        val item = baseKChartView.getItem(position) ?: return
        var text = "MACD:${getValueFormatter().format(item.macd)}   "
        var x0 = x
        canvas.drawText(text, x0, y, macdPaint)
        x0 += macdPaint.measureText(text)
        text = "DIF:${getValueFormatter().format(item.dif)}   "
        canvas.drawText(text, x0, y, difPaint)
        x0 += difPaint.measureText(text)
        text = "DEA:${getValueFormatter().format(item.dea)}"
        canvas.drawText(text, x0, y, deaPaint)
    }

    override fun getMaxValue(point: KEntity): Float {
        return max(point.macd, max(point.dea, point.dif))
    }

    override fun getMinValue(point: KEntity): Float {
        return min(point.macd, min(point.dea, point.dif))
    }

    init {
        setAttr()
    }

}