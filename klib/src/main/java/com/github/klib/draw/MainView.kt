package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.StringRes
import com.github.klib.BaseKChartView
import com.github.klib.R
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.util.DensityUtil
import kotlin.math.max
import kotlin.math.min

/**
 * 主图
 */
class MainView(private var baseKChartView: BaseKChartView) : IChartDraw<KEntity> {

    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
            candleUpPaint.color = candleUpColor
            candleDownPaint.color = candleDownColor
            selectorTextPaint.color = textColor
            selectorFramePaint.color = textColor
            selectorFramePaint.style = Paint.Style.STROKE

            ma5Paint.color = ma5Color
            ma10Paint.color = ma10Color
            ma30Paint.color = ma30Color
            selectorTextPaint.color = textColor
            selectorTextPaint.textSize = textSize
            selectorBackgroundPaint.color = selectorBackgroundColor

            ma5Paint.strokeWidth = lineWidth
            ma10Paint.strokeWidth = lineWidth
            ma30Paint.strokeWidth = lineWidth

            ma5Paint.textSize = textSize
            ma10Paint.textSize = textSize
            ma30Paint.textSize = textSize
        }

    }

    //跌时蜡烛颜色（默认红）
    private val candleDownPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //涨时蜡烛颜色（默认绿）
    private val candleUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    //指标
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma30Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    //选中状态下的绘制
    private val selectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 是否显示ma/ boll
     */
    private var showMa = true//默认显示ma
    private var showBoll = false
    /**
     *
     */
    fun showMaAndBoll(showMa: Boolean? = null, showBoll: Boolean? = null) {
        showBoll?.let {
            this.showBoll = it
        }
        showMa?.let {
            this@MainView.showMa = it
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
        drawCandle(canvas, currX, currPoint)
        if (showMa) {
            if (lastPoint.MA5 != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.MA5),
                    currX,
                    baseKChartView.getMainY(currPoint.MA5),
                    ma5Paint
                )
            }
            if (lastPoint.MA10 != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.MA10),
                    currX,
                    baseKChartView.getMainY(currPoint.MA10),
                    ma10Paint
                )
            }
            if (lastPoint.MA30 != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.MA30),
                    currX,
                    baseKChartView.getMainY(currPoint.MA30),
                    ma30Paint
                )
            }
        } else if (showBoll) {
            if (lastPoint.up != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.up),
                    currX,
                    baseKChartView.getMainY(currPoint.up),
                    ma5Paint
                )
            }
            if (lastPoint.mb != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.mb),
                    currX,
                    baseKChartView.getMainY(currPoint.mb),
                    ma10Paint
                )
            }
            if (lastPoint.dn != 0f) {
                canvas.drawLine(
                    lastX,
                    baseKChartView.getMainY(lastPoint.dn),
                    currX,
                    baseKChartView.getMainY(currPoint.dn),
                    ma30Paint
                )
            }
        }


    }

    /**
     * 画蜡烛
     */
    private fun drawCandle(canvas: Canvas, currX: Float, currPoint: KEntity) {
        val high = baseKChartView.getMainY(currPoint.highest)
        val low = baseKChartView.getMainY(currPoint.lowest)
        val open = baseKChartView.getMainY(currPoint.open)//开盘价
        val close = baseKChartView.getMainY(currPoint.close)//上一个的收盘价
        val r = baseKChartView.klineAttribute.candleWidth / 2
        val lineR = baseKChartView.klineAttribute.candleLineWidth / 2
        when {
            currPoint.open > currPoint.close -> {//阴线
                //            if (candleSolid) {
                canvas.drawRect(currX - r, close, currX + r, open, candleDownPaint)
                canvas.drawRect(currX - lineR, high, currX + lineR, low, candleDownPaint)
                //            }
            }
            currPoint.open < currPoint.close -> {//阳
                canvas.drawRect(currX - r, open, currX + r, close, candleUpPaint)
                canvas.drawRect(currX - lineR, high, currX + lineR, low, candleUpPaint)
            }
            else -> {
                canvas.drawRect(currX - r, open, currX + r, close + 1, candleDownPaint)
                canvas.drawRect(currX - lineR, high, currX + lineR, low, candleDownPaint)
            }
        }


    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        val item = baseKChartView.getItem(position) ?: return
        var x0 = x
        if (showMa) {
            var text = "MA5:${baseKChartView.formatValue(item.MA5)} "
            canvas.drawText(text, x0, y, ma5Paint)
            x0 += ma5Paint.measureText("$text   ")
            text = "MA10:${baseKChartView.formatValue(item.MA10)} "
            canvas.drawText(text, x0, y, ma10Paint)
            x0 += ma10Paint.measureText("$text   ")
            text = "MA30:${baseKChartView.formatValue(item.MA30)}"
            canvas.drawText(text, x0, y, ma30Paint)
        } else if (showBoll) {
            var text = "BOLL:${baseKChartView.formatValue(item.mb)} "
            canvas.drawText(text, x0, y, ma10Paint)
            x0 += ma5Paint.measureText("$text   ")
            text = "UB:${baseKChartView.formatValue(item.up)} "
            canvas.drawText(text, x0, y, ma5Paint)
            x0 += ma10Paint.measureText("$text   ")
            text = "LB:${baseKChartView.formatValue(item.dn)}"
            canvas.drawText(text, x0, y, ma30Paint)
        }

        if (baseKChartView.isLongPress) {
            drawSelector(canvas)
        }
    }

    /**
     *绘制高亮部分
     */
    private fun drawSelector(canvas: Canvas) {
        val metrics = selectorTextPaint.fontMetrics
        val textH = metrics.descent - metrics.ascent
        val baseline = (textH - metrics.bottom - metrics.top) / 2
        val index = baseKChartView.mSelectedIndex
        val item = baseKChartView.getItem(index) ?: return
        val padding = DensityUtil.dip2px(baseKChartView.context, 5f)
        //外框对外边距
        @Suppress("UnnecessaryVariable") val margin = padding
        val xOfIndex = baseKChartView.translateXtoX(baseKChartView.getXByIndex(index))

        val width = max(
            max(
                selectorTextPaint.measureText("${getString(R.string.kline_time)}  ${item.dateTime}"),
                selectorTextPaint.measureText("${getString(R.string.kline_open)}  ${item.open}")
            ),
            selectorTextPaint.measureText("${getString(R.string.kline_amount)}  ${item.volume}")
        ) + padding * 2
        //边框左坐标
        val left = if (xOfIndex > baseKChartView.mWidth / 2) {//需要显示在左边
            margin.toFloat()
        } else {//需要显示在右边
            baseKChartView.mWidth - margin - width
        }
        val top = (margin + baseKChartView.mTopPadding).toFloat()
        val height = padding * 8 + textH * 8
        val rect = RectF(left, top, left + width, top + height)
        canvas.drawRoundRect(rect, 6f, 6f, selectorBackgroundPaint)
        canvas.drawRoundRect(rect, 6f, 6f, selectorFramePaint)

        val list = mutableListOf<Array<Any>>()//第一个放标识,第二个放值
        list.add(arrayOf(getString(R.string.kline_time), item.dateTime))
        list.add(arrayOf(getString(R.string.kline_open), item.open))
        list.add(arrayOf(getString(R.string.kline_high), item.highest))
        list.add(arrayOf(getString(R.string.kline_low), item.lowest))
        list.add(arrayOf(getString(R.string.kline_close), item.close))
        list.add(arrayOf(getString(R.string.kline_change), item.change))
        list.add(arrayOf(getString(R.string.kline_change_p), item.changePercent))
        list.add(arrayOf(getString(R.string.kline_amount), item.volume))

        val x = left + padding
        var y = top + padding + baseline
        for (i in 0 until list.size) {
            val arrItem = list[i]
            selectorTextPaint.color = baseKChartView.klineAttribute.textColor
            canvas.drawText("${arrItem[0]}", x, y, selectorTextPaint)
            if (i == 5 || i == 6) {//颜色变了。。。。
                val value = list[5][1] as Float//获取change
                selectorTextPaint.color =
                    if (value >= 0f) baseKChartView.klineAttribute.candleUpColor else baseKChartView.klineAttribute.candleDownColor
                canvas.drawText(
                    "${if (value >= 0f) "+" else "-"}${arrItem[1]}",
                    left + width - padding - selectorTextPaint.measureText("${arrItem[1]}"),
                    y,
                    selectorTextPaint
                )
            } else {
                canvas.drawText(
                    "${arrItem[1]}",
                    left + width - padding - selectorTextPaint.measureText("${arrItem[1]}"),
                    y,
                    selectorTextPaint
                )
            }

            y += textH + padding
        }


    }


    private fun getString(@StringRes resId: Int) = baseKChartView.context.getString(resId)

    override fun getMaxValue(point: KEntity): Float {
        return if (showMa) {
            max(point.highest, point.MA30)
        } else {
            //up
            if (point.up.isNaN()) {
                point.mb
            } else point.up
        }
    }

    override fun getMinValue(point: KEntity): Float {
        return if (showMa) {
            min(point.MA30, point.lowest)
        } else {
            if (point.dn.isNaN()) {
                point.mb
            } else {
                point.dn
            }
        }
    }


    init {
        setAttr()
    }


}