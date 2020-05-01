package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
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
    private val volTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius: Float = 0f

    override fun setAttr() {
        baseKchartView.klineAttribute.apply {
            volDownPaint.color = candleDownColor
            volUpPaint.color = candleUpColor
            volMa5Paint.color = ma5Color
            volMa10Paint.color = ma10Color
            volTextPaint.color = textColor

            volDownPaint.strokeWidth = candleWidth
            volUpPaint.strokeWidth = candleWidth

            volTextPaint.textSize = textSize

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
        if (lastPoint.ma5Volume != 0f && position >= 5) {
            canvas.drawLine(
                lastX,
                baseKchartView.getVolumeY(lastPoint.ma5Volume),
                currX,
                baseKchartView.getVolumeY(currPoint.ma5Volume),
                volMa5Paint
            )
        }
        if (lastPoint.ma10Volume != 0f && position >= 10) {
            canvas.drawLine(
                lastX,
                baseKchartView.getVolumeY(lastPoint.ma10Volume),
                currX,
                baseKchartView.getVolumeY(currPoint.ma10Volume),
                volMa10Paint
            )
        }
    }

    //绘制量
    private fun drawVol(currPoint: KEntity, currX: Float, canvas: Canvas) {
        val paint = if (currPoint.close >= currPoint.open) {
            volUpPaint
        } else {
            volDownPaint
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
        val item = baseKchartView.getItem(position) ?: return
        var x0 = x
        var text = "VOL:${getValueFormatter().format(item.volume)}"
        volTextPaint.color = baseKchartView.klineAttribute.textColor
        canvas.drawText(text, x0, y, volTextPaint)
        x0 += volTextPaint.measureText("$text   ")
        text = "MA5:${getValueFormatter().format(item.ma5Volume)}"
        volTextPaint.color = baseKchartView.klineAttribute.ma5Color
        canvas.drawText(text, x0, y, volTextPaint)
        x0 += volTextPaint.measureText("$text   ")
        text = "MA10:${getValueFormatter().format(item.ma10Volume)}"
        volTextPaint.color = baseKchartView.klineAttribute.ma10Color
        canvas.drawText(text, x0, y, volTextPaint)
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