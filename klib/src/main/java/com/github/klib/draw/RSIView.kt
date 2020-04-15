package com.github.klib.draw

import android.graphics.Canvas
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter

class RSIView: IChartDraw<KEntity> {
    override fun drawTranslated(
        lastPoint: KEntity,
        currPoint: KEntity,
        lastX: Float,
        currX: Float,
        canvas: Canvas,
        position: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMaxValue(point: KEntity): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMinValue(point: KEntity): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getValueFormatter(): IValueFormatter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}