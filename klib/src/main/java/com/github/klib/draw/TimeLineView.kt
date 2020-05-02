package com.github.klib.draw

import android.graphics.*
import androidx.annotation.StringRes
import com.github.klib.BaseKChartView
import com.github.klib.R
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.IChartDraw
import com.github.klib.util.DensityUtil
import kotlin.math.max

class TimeLineView(private val baseKChartView: BaseKChartView) : IChartDraw<KEntity> {
    //顶部折线画笔
    private val timeLinePaint = Paint()
    //阴影区域画笔
    private val shaderPaint = Paint()

    //路径
    private val timeLinePath = Path()
    private val shaderPath = Path()

    //选中状态下的绘制
    private val selectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun setAttr() {
        baseKChartView.klineAttribute.apply {
            timeLinePaint.strokeWidth = timeLineWidth
            timeLinePaint.color = timeLineColor
            timeLinePaint.style = Paint.Style.STROKE

            selectorTextPaint.color = textColor
            selectorTextPaint.textSize = textSize
            selectorBackgroundPaint.color = selectorBackgroundColor
            selectorFramePaint.color = textColor
            selectorFramePaint.style = Paint.Style.STROKE
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
        val currY = baseKChartView.getMainY(currPoint.close)
        val mainRectBtm = baseKChartView.mMainRect.bottom.toFloat()


        when (position) {
            baseKChartView.mStartIndex -> {
                shaderPaint.shader = LinearGradient(
                    0f, baseKChartView.mMainRect.top.toFloat(),
                    0f, mainRectBtm,
                    intArrayOf(
                        baseKChartView.klineAttribute.timeLineShaderColorTop,
                        baseKChartView.klineAttribute.timeLineShaderColorBtm
                    ),
                    null, Shader.TileMode.CLAMP
                )

                timeLinePath.moveTo(currX, currY)

                shaderPath.moveTo(currX, mainRectBtm)
                shaderPath.lineTo(currX, currY)
            }
            baseKChartView.mStopIndex -> {
                timeLinePath.lineTo(currX, currY)

                shaderPath.lineTo(currX, currY)
                shaderPath.lineTo(currX, mainRectBtm)
                shaderPath.close()

                //

                canvas.drawPath(shaderPath, shaderPaint)
                canvas.drawPath(timeLinePath, timeLinePaint)
                timeLinePath.reset()
                shaderPath.reset()
            }
            else -> {
                timeLinePath.lineTo(currX, currY)

                shaderPath.lineTo(currX, currY)
            }
        }

    }

    private fun getString(@StringRes resId: Int) = baseKChartView.context.getString(resId)

    override fun drawText(canvas: Canvas, position: Int, x: Float, y: Float) {

        if (baseKChartView.isLongPress) {//绘制高亮
            val metrics = selectorTextPaint.fontMetrics
            val textH = metrics.descent - metrics.ascent
            val baseline = (textH - metrics.bottom - metrics.top) / 2
            val index = baseKChartView.mSelectedIndex
            val item = baseKChartView.getItem(index) ?: return
            val padding = DensityUtil.dip2px(baseKChartView.context, 5f)
            //外框对外边距
            @Suppress("UnnecessaryVariable") val margin = padding
            val xOfIndex = baseKChartView.translateXtoX(baseKChartView.getXByIndex(index))

            val width =
                max(
                    selectorTextPaint.measureText("${getString(R.string.kline_time)}  ${item.dateTime}"),
                    selectorTextPaint.measureText("${getString(R.string.kline_open)}  ${item.close}")
                ) + padding * 2
            //边框左坐标
            val left = if (xOfIndex > baseKChartView.mWidth / 2) {//需要显示在左边
                margin.toFloat()
            } else {//需要显示在右边
                baseKChartView.mWidth - margin - width
            }
            val top = (margin + baseKChartView.mTopPadding).toFloat()
            val height = padding * 2 + textH
            val rect = RectF(left, top, left + width, top + height)
            canvas.drawRoundRect(rect, 6f, 6f, selectorBackgroundPaint)
            canvas.drawRoundRect(rect, 6f, 6f, selectorFramePaint)

            val list = mutableListOf<Array<Any>>()//第一个放标识,第二个放值
            list.add(arrayOf(getString(R.string.kline_close), item.close))
            val x0 = left + padding
            var y0 = top + padding + baseline
            for (i in 0 until list.size) {
                val arrItem = list[i]
                selectorTextPaint.color = baseKChartView.klineAttribute.textColor
                canvas.drawText("${arrItem[0]}", x0, y0, selectorTextPaint)
                val textVal = if (arrItem[1] is Float) {
                    baseKChartView.formatValue(arrItem[1] as Float)
                } else {
                    "${arrItem[1]}"
                }
                canvas.drawText(
                    textVal,
                    left + width - padding - selectorTextPaint.measureText(textVal),
                    y0,
                    selectorTextPaint
                )
                y0 += textH + padding
            }

        }
    }

    override fun getMaxValue(point: KEntity) = point.highest


    override fun getMinValue(point: KEntity) = point.lowest


    init {
        setAttr()
    }

}