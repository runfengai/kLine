package com.github.klib.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.StringRes
import com.github.klib.BaseKChartView
import com.github.klib.R
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import com.github.klib.util.DensityUtil
import java.lang.StringBuilder
import kotlin.math.max
import kotlin.math.min

/**
 * 主图
 */
class MainView(private var baseKChartView: BaseKChartView) : IChartDraw<KEntity> {
    init {
        setAttr(baseKChartView)
    }
    private fun setAttr(baseKChartView: BaseKChartView) {
        candleUpPaint.color = baseKChartView.kLineAttribute.candleUpColor
        candleDownPaint.color = baseKChartView.kLineAttribute.candleUpColor
        selectorTextPaint.color = baseKChartView.kLineAttribute.textColor
        selectorFramePaint.color = baseKChartView.kLineAttribute.textColor
        selectorFramePaint.style = Paint.Style.STROKE

        ma5Paint.color = baseKChartView.kLineAttribute.ma5Color
        ma10Paint.color = baseKChartView.kLineAttribute.ma10Color
        ma30Paint.color = baseKChartView.kLineAttribute.ma30Color
        selectorTextPaint.color = baseKChartView.kLineAttribute.textColor
        selectorTextPaint.textSize = baseKChartView.kLineAttribute.textSize
        selectorBackgroundPaint.color = baseKChartView.kLineAttribute.selectorBackgroundColor

        ma5Paint.strokeWidth = baseKChartView.kLineAttribute.lineWidth
        ma10Paint.strokeWidth = baseKChartView.kLineAttribute.lineWidth
        ma30Paint.strokeWidth = baseKChartView.kLineAttribute.lineWidth
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
    var showMa = true//默认显示ma
    var showBoll = false
    /**
     *
     */
    fun showMaAndBoll(showMa: Boolean, showBoll: Boolean) {
        this.showBoll = showBoll
        this.showMa = showMa
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
        val r = baseKChartView.kLineAttribute.candleWidth / 2
        val lineR = baseKChartView.kLineAttribute.candleLineWidth / 2
        if (open > close) {//阴线
//            if (baseKchartView.kLineAttribute.candleSolid) {
            //todo 搞
            canvas.drawRect(currX - r, close, currX + r, open, candleDownPaint)
            canvas.drawRect(currX - lineR, high, currX + lineR, low, candleDownPaint)
//            }
        } else if (open < close) {//阳
            canvas.drawRect(currX - r, open, currX + r, close, candleUpPaint)
            canvas.drawRect(currX - lineR, high, currX + lineR, low, candleUpPaint)
        } else {
            canvas.drawRect(currX - r, open, currX + r, close + 1, candleDownPaint)
            canvas.drawRect(currX - lineR, high, currX + lineR, low, candleDownPaint)
        }


    }

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {
        val item = baseKChartView.getItem(position) ?: return
        var x0 = x
        if (showMa) {
            var text = "MA5:${baseKChartView.formatValue(item.MA5)} "
            canvas.drawText(text, x0, y, ma5Paint)
            x0 += ma5Paint.measureText(text)
            text = "MA10:${baseKChartView.formatValue(item.MA10)} "
            canvas.drawText(text, x0, y, ma10Paint)
            x0 += ma10Paint.measureText(text)
            text = "MA30:${baseKChartView.formatValue(item.MA30)}"
            canvas.drawText(text, x0, y, ma30Paint)
        } else if (showBoll) {
            var text = "BOLL:${baseKChartView.formatValue(item.mb)} "
            canvas.drawText(text, x0, y, ma10Paint)
            x0 += ma10Paint.measureText(text)
            text = "UB:${baseKChartView.formatValue(item.up)} "
            canvas.drawText(text, x0, y, ma5Paint)
            x0 += ma5Paint.measureText(text)
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
        val textHeight = metrics.descent - metrics.ascent
        val index = baseKChartView.mSelectedIndex

        val padding = DensityUtil.dip2px(baseKChartView.context, 5f)
        val margin = padding
        val width: Float//取宽度最大值
        val left: Float
        val top = margin + baseKChartView.mTopPadding
        val height = padding * 8 + textHeight * 8
        val item = baseKChartView.getItem(index) ?: return
        val adapter = baseKChartView.getAdapter() ?: return

        val list = mutableListOf<Array<Any>>()//第一个放标识,第二个放值
        val textWidths = mutableListOf<Float>()
        list.add(arrayOf(getString(R.string.kline_time), adapter.getDate(index)))
        list.add(arrayOf(getString(R.string.kline_open), item.open))
        list.add(arrayOf(getString(R.string.kline_high), item.highest))
        list.add(arrayOf(getString(R.string.kline_low), item.lowest))
        list.add(arrayOf(getString(R.string.kline_close), item.close))
        list.add(arrayOf(getString(R.string.kline_change), item.change))
        list.add(arrayOf(getString(R.string.kline_change_p), item.changePercent))
        list.add(arrayOf(getString(R.string.kline_amount), item.volume))
        var maxW = 0f//计算文字中最大
        list.forEach {
            val textW = selectorTextPaint.measureText("${it[0]}${it[1]}")
            textWidths.add(textW)
            maxW = max(maxW, textW)
        }
        val spaceW = selectorFramePaint.measureText(" ")//空格宽度
        width = maxW + (padding + spaceW) * 2
        val x = baseKChartView.translateXtoX(baseKChartView.getXByIndex(index))
        left = if (x > baseKChartView.mWidth / 2) {//需要显示在左边
            margin.toFloat()
        } else {//需要显示在右边
            baseKChartView.mWidth - margin - width
        }
        val rect = RectF(left, top.toFloat(), left + width, top + height)
        canvas.drawRoundRect(rect, 6f, 6f, selectorBackgroundPaint)
        canvas.drawRoundRect(rect, 6f, 6f, selectorFramePaint)
        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        for (i in 0 until list.size) {
            val it = list[i]
            val textW = textWidths[i]
            var spaceCount = ((maxW - textW) / spaceW).toInt()
            if (spaceCount < 0) spaceCount = 0
            val sb = StringBuilder("  ")
            for (j in 0 until spaceCount) {
                sb.append(" ")
            }
            if (i == 5 || i == 6) {//颜色变了。。。。
                var redX = x + padding
                val redY = y
                val textLeft = "${it[0]}$sb"
                canvas.drawText(textLeft, redX, redY, selectorFramePaint)
                redX += selectorFramePaint.measureText(textLeft)
                val value = it[1] as Float
                selectorFramePaint.color =
                    if (value > 0f) baseKChartView.kLineAttribute.candleUpColor else baseKChartView.kLineAttribute.candleDownColor
                canvas.drawText("${it[1]}", redX, redY, selectorFramePaint)
            } else {
                selectorFramePaint.color = baseKChartView.kLineAttribute.textColor
                val text = "${it[0]}${sb}${it[1]}"
                canvas.drawText(text, x + padding, y, selectorFramePaint)
                y += textHeight + padding
            }
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

    override fun getValueFormatter(): IValueFormatter = DefValueFormatter()




}