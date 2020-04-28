package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.BigValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.max
import kotlin.math.min

class VolumeView(private var baseKchartView: BaseKChartView) : IChartDraw<KEntity> {
    private val volDownPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val volUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val volMa5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val volMa10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius: Float = 0f
    private fun setAttr() {
        baseKchartView.klineAttribute.apply {
            volDownPaint.color = candleDownColor
            volUpPaint.color = candleUpColor
            volMa5Paint.color = ma5Color
            volMa10Paint.color = ma10Color
            volDownPaint.strokeWidth = candleWidth
            volUpPaint.strokeWidth = candleWidth
            radius = candleWidth / 2
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
        drawVol(currPoint, currX, canvas)
        //绘制ma5,ma10
        canvas.drawLine(
            lastX,
            baseKchartView.getVolumeY(lastPoint.ma5Volume),
            currX,
            baseKchartView.getVolumeY(currPoint.ma5Volume),
            volMa5Paint
        )
        canvas.drawLine(
            lastX,
            baseKchartView.getVolumeY(lastPoint.ma10Volume),
            currX,
            baseKchartView.getVolumeY(currPoint.ma10Volume),
            volMa10Paint
        )
    }

    //绘制量
    private fun drawVol(currPoint: KEntity, currX: Float, canvas: Canvas) {
        val paint = if (currPoint.close >= currPoint.open) {
            volDownPaint
        } else {
            volUpPaint
        }
        canvas.drawRect(
            currX - radius,
            baseKchartView.getVolumeY(currPoint.volume),
            currX + radius,
            baseKchartView.mVolumeRect.bottom.toFloat(),
            paint
        )
    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: KEntity): Float {
        return max(point.volume, max(point.ma5Volume, point.ma10Volume))
    }

    override fun getMinValue(point: KEntity): Float {
        return min(point.volume, min(point.ma5Volume, point.ma10Volume))
    }

    override fun getValueFormatter(): IValueFormatter {
        return BigValueFormatter(baseKchartView.context)
    }

    init {
        setAttr()
    }

}