package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import kotlin.math.max
import kotlin.math.min

class MACDView(private var baseKchartView: BaseKChartView) : IChartDraw<KEntity> {
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
        baseKchartView.klineAttribute.apply {
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
            baseKchartView.getSubY(lastPoint.dif),
            currX,
            baseKchartView.getSubY(currPoint.dif),
            difPaint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getSubY(lastPoint.dea),
            currX,
            baseKchartView.getSubY(currPoint.dea),
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
            baseKchartView.getSubY(macd),
            currX + radius,
            baseKchartView.getSubY(0f),
            paint
        )

    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

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