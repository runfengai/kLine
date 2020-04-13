package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter

class BollView : IChartDraw<KEntity> {
    private var baseKChartView: BaseKChartView

    constructor(baseKChartView: BaseKChartView) {
        this.baseKChartView = baseKChartView
    }

    /**
     * 三个轨道
     */
    private val mUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDnPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(
        lastPoint: KEntity,
        currPoint: KEntity,
        lastX: Float,
        currX: Float,
        canvas: Canvas,
        position: Int
    ) {
        baseKChartView.drawChildLine(canvas, mUpPaint, lastX, lastPoint.up, currX, currPoint.up)
        baseKChartView.drawChildLine(canvas, mMbPaint, lastX, lastPoint.mb, currX, currPoint.mb)
        baseKChartView.drawChildLine(canvas, mDnPaint, lastX, lastPoint.dn, currX, currPoint.dn)
    }

    override fun drawText(
        canvas: Canvas,
        position: Int,
        x: Float,
        y: Float
    ) {
        var currX = x
        var text: String
        val item = baseKChartView.getItem(position)
        text = "UP:${baseKChartView.formatValue(item?.up ?: 0f)} "
        canvas.drawText(text, currX, y, mUpPaint)
        currX += mUpPaint.measureText(text)
        text = "MB:${baseKChartView.formatValue(item?.mb ?: 0f)} "
        canvas.drawText(text, currX, y, mMbPaint)
        currX += mMbPaint.measureText(text)
        text = "DN:${baseKChartView.formatValue(item?.dn ?: 0f)} "
        canvas.drawText(text, currX, y, mDnPaint)
    }

    /**
     * 上轨中寻找最大值
     */
    override fun getMaxValue(point: KEntity): Float {
        if (point.up.isNaN()) {
            return point.mb
        }
        return point.up
    }

    override fun getMinValue(point: KEntity): Float {
        if (point.dn.isNaN()) {
            return point.mb
        }
        return point.dn
    }

    override fun getValueFormatter(): IValueFormatter {
        return baseKChartView.mValueFormatter
    }

}