package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.klib.BaseKChartView
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw

class WRView(private var baseKChartView: BaseKChartView) : IChartDraw<KEntity> {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
            paint.strokeWidth = lineWidth
            paint.color = wrColor
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
            baseKChartView.getSubY(lastPoint.wr),
            currX,
            baseKChartView.getSubY(currPoint.wr),
            paint
        )
    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {}

    override fun getMaxValue(point: KEntity): Float {
        return point.wr
    }

    override fun getMinValue(point: KEntity): Float {
        return point.wr
    }


    init {
        setAttr()
    }
}