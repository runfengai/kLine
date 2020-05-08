package com.github.klib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.github.klib.entity.DepthEntity
import com.github.klib.util.DensityUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 深度图,弃用
 */
class DepthView : View {


    private val buyList = mutableListOf<DepthEntity>()
    private val sellList = mutableListOf<DepthEntity>()

    private val buyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buyFillPaint = Paint()
    private val sellFillPaint = Paint()
    //线
    private val buyPath = Path()
    private val sellPath = Path()
    //Fill区域
    private val buyFillPath = Path()
    private val sellFillPath = Path()
    /**
     * 计算的各种指标
     *
     */
    private var buyPriceMin = 0f
    private var buyPriceMax = 0f
    private var sellPriceMin = 0f
    private var sellPriceMax = 0f

    private var priceMiddle = 0f

    private var buyVolumeMin = 0f
    private var buyVolumeMax = 0f
    private var sellVolumeMin = 0f
    private var sellVolumeMax = 0f


    private var buyColor: Int = 0
    private var buyFillColor: Int = 0
    private var sellColor: Int = 0
    private var sellFillColor: Int = 0
    private var lineWidth: Float = 0f
    //文字大小
    private var textSize: Float = 0f
    private var textColor: Int = 0

    /**
     * 顶部绘制矩形，绘制文字
     */
    private var topRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var topRectSize = 0f
    private var topTextPadding = 0
    //长按选中后圆圈
    private var depthCircleRadius: Float = 0f


    var gridRows = 5
    var gridColumns = 5


    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.apply {
            initView(this)
        }
    }

    constructor(context: Context, attrs: AttributeSet?, styleAttr: Int) : super(
        context,
        attrs,
        styleAttr
    ) {
        attrs?.apply {
            initView(this)
        }
    }

    private var buyText = ""
    private var sellText = ""
    private fun initView(attributeSet: AttributeSet?) {
        attributeSet?.apply {
            setAttrs(this)
        }
        mTopWidth = getDimension(R.dimen.kline_depth_top_Width)
        mBottomPriceHeight = getDimension(R.dimen.kline_padding).toInt()
        topRectSize = getDimension(R.dimen.kline_depth_top_rect_size)
        topTextPadding = DensityUtil.dip2px(context, 6f)
        buyText = context.getString(R.string.kline_depth_buy)
        sellText = context.getString(R.string.kline_depth_sell)
    }

    private fun setAttrs(attributeSet: AttributeSet) {
        val attrs = context.obtainStyledAttributes(attributeSet, R.styleable.DepthView)
        attrs.apply {
            buyColor =
                getColor(R.styleable.DepthView_buyColor, getColor(R.color.kline_depth_buy_color))
            buyFillColor = getColor(
                R.styleable.DepthView_buyFillColor,
                getColor(R.color.kline_depth_buy_fill_color)
            )
            sellColor =
                getColor(R.styleable.DepthView_sellColor, getColor(R.color.kline_depth_sell_color))
            sellFillColor = getColor(
                R.styleable.DepthView_sellFillColor,
                getColor(R.color.kline_depth_sell_fill_color)
            )
            lineWidth = getDimension(
                R.styleable.DepthView_topLineWidth,
                getDimension(R.dimen.kline_depth_line_width)
            )
            this@DepthView.gridRows = getInteger(R.styleable.DepthView_gridRows, 5)
            gridColumns = getInteger(R.styleable.DepthView_gridColumns, 4)

            mGridPaint.color =
                getColor(
                    R.styleable.DepthView_depth_gridLineColor,
                    getColor(R.color.kline_grid_line)
                )
            mGridPaint.strokeWidth = getDimension(
                R.styleable.DepthView_depth_gridLineWidth,
                getDimension(R.dimen.kline_grid_line_width)
            )
            textSize = getDimension(
                R.styleable.DepthView_depthTextSize,
                getDimension(R.dimen.kline_depth_text_size)
            )
            textColor = getColor(
                R.styleable.DepthView_depthTextColor,
                getColor(R.color.kline_depth_text_color)
            )

            depthCircleRadius = getDimension(
                R.styleable.DepthView_depthCircleRadius,
                getDimension(R.dimen.kline_depth_circle_radius)
            )

            textPaint.textSize = textSize
            textPaint.color = textColor

            buyPaint.color = buyColor
            buyPaint.strokeWidth = lineWidth
            buyPaint.style = Paint.Style.STROKE
            buyFillPaint.color = buyFillColor
            sellPaint.color = sellColor
            sellPaint.strokeWidth = lineWidth
            sellPaint.style = Paint.Style.STROKE
            sellFillPaint.color = sellFillColor

        }
        attrs.recycle()
    }


    fun setData(buyList: List<DepthEntity>, sellList: List<DepthEntity>) {
        setBuyList(buyList)
        setSellList(sellList)

        requestLayout()
    }

    private fun setSellList(sellList: List<DepthEntity>) {
        this.sellList.clear()
        this.sellList.addAll(sellList.sorted())
    }

    private fun setBuyList(buyList: List<DepthEntity>) {
        this.buyList.clear()
        this.buyList.addAll(buyList.sorted())
    }

    /**
     * 各种计算
     */
    private fun calculate() {
        buyPriceMin = Float.MAX_VALUE
        buyPriceMax = Float.MIN_VALUE
        sellPriceMin = Float.MAX_VALUE
        sellPriceMax = Float.MIN_VALUE
        for (i in buyList.size - 1 downTo 0) {
            buyPriceMin = min(buyPriceMin, buyList[i].price)
            buyPriceMax = max(buyPriceMax, buyList[i].price)
            if (i < buyList.size - 1) {
                buyList[i].amount += buyList[i + 1].amount
            } else {//最后一个，也就是price最高
                buyVolumeMin = buyList[i].amount
            }
            if (i == 0) {//算总的volume
                buyVolumeMax = buyList[i].amount
            }
        }

        for (i in 0 until sellList.size) {
            sellPriceMin = min(sellPriceMin, sellList[i].price)
            sellPriceMax = max(sellPriceMax, sellList[i].price)
            if (i > 0) {
                sellList[i].amount += sellList[i - 1].amount
            } else {
                sellVolumeMin = sellList[i].amount
            }
            if (i == sellList.size - 1) {
                sellVolumeMax = sellList[i].amount
            }

        }

        priceMiddle = buyPriceMax / 2 + sellPriceMin / 2

        //计算volume
        val drawHeight = mHeight - mTopWidth - mBottomPriceHeight
        volumeSpace = drawHeight / this.gridRows
        volumeMax = max(buyVolumeMax, sellVolumeMax)
        val volumeMin = min(buyVolumeMin, sellVolumeMin)
        volumeItem = volumeMax / this.gridRows //固定的
        //每一单位volume对应的高度
        val avgHeightPerVolume = drawHeight / (volumeMax - volumeMin)
        val avgWidthPreSize = mWidth.toFloat() / (buyList.size + sellList.size)

        //计算x,y坐标并赋值
        for (i in 0 until buyList.size) {
            buyList[i].x = avgWidthPreSize * i
            buyList[i].y = mTopWidth + (volumeMax - buyList[i].amount) * avgHeightPerVolume
        }
        //计算x,y坐标并赋值
        for (i in 0 until sellList.size) {
            sellList[i].x = mWidth - (sellList.size - 1 - i) * avgWidthPreSize
            sellList[i].y = mTopWidth + (volumeMax - sellList[i].amount) * avgHeightPerVolume
        }

    }

    private var mWidth = 0f
    private var mHeight = 0f
    /**
     * 精度
     */
    var scale = 2
    /**
     * 顶部，需要留出位置显示买卖方向
     */
    var mTopWidth = 0f
    private var leftStart: Float = 0f
    private var topStart: Float = 0f
    private var rightEnd: Float = 0f
    private var bottomEnd: Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        calculate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        leftStart = (paddingLeft + 1).toFloat()
        topStart = (paddingTop + 1).toFloat()
        rightEnd = (measuredWidth - paddingRight - 1).toFloat()
        bottomEnd = (measuredHeight - paddingBottom - 1).toFloat()


    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        drawGrid(canvas)
        drawValue(canvas)
        drawTop(canvas)
        drawLeft(canvas)
        drawRight(canvas)
        drawSelected(canvas)
    }


    var showLeft = true
    var showRight = false
    //volume最大值
    private var volumeMax: Float = 0f
    //显示用的
    private var volumeItem: Float = 0f
    private var volumeSpace: Float = 0f


    /**
     * 绘制value
     */
    private fun drawValue(canvas: Canvas) {


        for (i in this.gridRows downTo 1) {
            if (showLeft) {
                canvas.drawText(
                    getFormatValue(volumeMax - (i - 1) * volumeItem),
                    topTextPadding.toFloat(),
                    mTopWidth + volumeSpace * (i - 1),
                    textPaint
                )
            }
            if (showRight) {
                val text = getFormatValue(volumeMax - (i - 1) * volumeItem)
                canvas.drawText(
                    text,
                    mWidth - textPaint.measureText(text) - topTextPadding.toFloat(),
                    mTopWidth + volumeSpace * (i - 1),
                    textPaint
                )
            }

        }
        val metrics = textPaint.fontMetrics
        val textH = metrics.descent - metrics.ascent
        canvas.drawText(
            getFormatValue(buyPriceMin),
            topTextPadding.toFloat(),
            mHeight - mBottomPriceHeight + textH,
            textPaint
        )
        val middleText = getFormatValue(priceMiddle)
        canvas.drawText(
            middleText,
            mWidth / 2 - textPaint.measureText(middleText) / 2,
            mHeight - mBottomPriceHeight + textH,
            textPaint
        )
        val endText = getFormatValue(sellPriceMax)
        canvas.drawText(
            endText,
            mWidth - textPaint.measureText(endText) - topTextPadding.toFloat(),
            mHeight - mBottomPriceHeight + textH,
            textPaint
        )

    }

    /**
     * 顶部买卖
     */
    private fun drawTop(canvas: Canvas) {
        topRectPaint.color = buyColor
        val buyTextWidth = textPaint.measureText(buyText)
        val metrics = textPaint.fontMetrics
//        val textH = metrics.descent - metrics.ascent
//        val baseLine = (textH - metrics.bottom - metrics.top) / 2
//        val baseLineToMidH = textH / 2
        val startXLeft = mWidth / 2 - topRectSize - topTextPadding * 2 - buyTextWidth

        canvas.drawRect(
            RectF(
                startXLeft,
                mTopWidth / 2 - topRectSize / 2,
                startXLeft + topRectSize,
                mTopWidth / 2 + topRectSize / 2
            ), topRectPaint
        )
        canvas.drawText(
            buyText,
            startXLeft + topRectSize + topTextPadding,
            mTopWidth / 2 + metrics.bottom,
            textPaint
        )
        //卖
        val startXRight = mWidth / 2f + topTextPadding
        topRectPaint.color = sellColor
        canvas.drawRect(
            RectF(
                startXRight,
                mTopWidth / 2 - topRectSize / 2,
                startXRight + topRectSize,
                mTopWidth / 2 + topRectSize / 2
            ), topRectPaint
        )

        canvas.drawText(
            sellText,
            startXRight + topRectSize + topTextPadding,
            mTopWidth / 2 + metrics.bottom,
            textPaint
        )

    }


    /**
     * 绘制买区域
     */
    private fun drawLeft(canvas: Canvas) {
        for (i in 0 until buyList.size) {

            when (i) {
                0 -> {
                    buyPath.moveTo(
                        buyList[i].x,
                        buyList[i].y
                    )

                    buyFillPath.moveTo(
                        buyList[i].x
                        ,
                        (mHeight - mBottomPriceHeight)
                    )
                    buyFillPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )
                }
                buyList.size - 1 -> {
                    buyPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )

                    buyFillPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )

                    buyFillPath.lineTo(buyList[i].x, mHeight - mBottomPriceHeight)
                    buyFillPath.close()

                    canvas.drawPath(buyFillPath, buyFillPaint)
                    canvas.drawPath(buyPath, buyPaint)

                    buyFillPath.reset()
                    buyPath.reset()
                }
                else -> {
                    buyPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )
                    buyFillPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )
                }
            }
        }
    }

    /**
     * 绘制卖区域
     */
    private fun drawRight(canvas: Canvas) {
        for (i in 0 until sellList.size) {
            when (i) {
                0 -> {
                    sellPath.moveTo(
                        sellList[i].x,
                        sellList[i].y
                    )

                    sellFillPath.moveTo(sellList[i].x, mHeight - mBottomPriceHeight)
                    sellFillPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )


                }
                sellList.size - 1 -> {
                    sellPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )

                    sellFillPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )
                    sellFillPath.lineTo(sellList[i].x, mHeight - mBottomPriceHeight)
                    sellFillPath.close()

                    canvas.drawPath(sellFillPath, sellFillPaint)
                    canvas.drawPath(sellPath, sellPaint)

                    sellFillPath.reset()
                    sellPath.reset()
                }
                else -> {
                    sellPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )
                    sellFillPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )
                }
            }
        }
    }

    private var rowSpace = 0f
    /**
     * 底部显示价格的区域高度
     */
    private var mBottomPriceHeight = 0
    //网格线
    private val mGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 绘制线
     */
    private fun drawGrid(canvas: Canvas) {
        if (this.gridRows > 0) {
            //        主图
            val widthF = mWidth.toFloat()
            val heightF = mHeight.toFloat()
            rowSpace =
                (heightF - mTopWidth - mBottomPriceHeight) / this.gridRows

            for (i in 0..this.gridRows) {
                val startY = rowSpace * i + mTopWidth
                canvas.drawLine(0f, startY, widthF, startY, mGridPaint)
            }

            val columnSpace = widthF / gridColumns
            for (i in 1 until gridColumns) {
                val startX = columnSpace * i.toFloat()
                canvas.drawLine(startX, 0f, startX, heightF - mBottomPriceHeight, mGridPaint)
            }
        }
    }

    private var inTouch: Boolean = false
    private var isLongPress: Boolean = false

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    //要高亮的位置
    private var highLightX: Float = 0f
    private var highLightY: Float = 0f

    //    private var touchPosition = 0
    private var isLeftTouch = true
    //长按的点对应在折线上的y坐标
    private var touchPositionOfLine = 0

    var startTime: Long = 0L

    private val LONG_PRESS_TIME = 500L

    private val TOUCH_MAX = 50

    private var mHandler = Handler()
    private val longPressedRunnable = Runnable {
        isLongPress = true
        highLightX = startX
        highLightY = startY
        getTopLineYByX(startX)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val ev = event ?: return super.onTouchEvent(event)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                inTouch = true
                startTime = System.currentTimeMillis()
                startX = ev.x
                startY = ev.y
                mHandler.postDelayed(longPressedRunnable, LONG_PRESS_TIME)
            }
            MotionEvent.ACTION_MOVE -> {
                touchX = ev.x
                touchY = ev.y
                if (abs(touchX - startX) > TOUCH_MAX) {
                    mHandler.removeCallbacks(longPressedRunnable)
                    getTopLineYByX(ev.x)
                    highLightX = touchX
                    highLightY = touchY
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                mHandler.removeCallbacks(longPressedRunnable)
                val endTime = System.currentTimeMillis()
                isLongPress = endTime > startTime + LONG_PRESS_TIME
                inTouch = false
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                inTouch = false
                isLongPress = false
                invalidate()
            }
        }

        return true
    }

    /**
     * 获取触摸的位置
     */
    private fun getTopLineYByX(x: Float) {
        when {//左边区域
            buyList.size > 0 && x <= buyList[buyList.size - 1].x -> {
                isLeftTouch = true
                touchPositionOfLine = getLeftPositionByX(x)
            }
            else -> {
                isLeftTouch = false
                touchPositionOfLine = getRightPositionByX(x)
            }
        }
    }

    /**
     * 绘制选中区域
     */
    private fun drawSelected(canvas: Canvas) {
        if (inTouch or isLongPress) {
            val paint = if (isLeftTouch) buyPaint else sellPaint
            val list = if (isLeftTouch) buyList else sellList
            canvas.drawCircle(list[touchPositionOfLine].x, list[touchPositionOfLine].y, depthCircleRadius, paint)
        }
    }

    /**
     * 根据x坐标获取对应position
     */
    private fun getLeftPositionByX(x: Float): Int {
        //二分查找
        return searchIndex(buyList, x)
    }

    /**
     * 根据x坐标获取对应position
     */
    private fun getRightPositionByX(x: Float): Int {
        return searchIndex(sellList, x)
    }

    /**
     * 二分查找 点对应的position
     */
    private fun searchIndex(list: List<DepthEntity>, x: Float): Int {
        return searchIndex(list, 0, list.size - 1, x)
    }

    private fun searchIndex(
        list: List<DepthEntity>,
        startIndex: Int,
        endIndex: Int, x: Float
    ): Int {
        if (endIndex <= startIndex + 1) {
            return startIndex
        }
        val mid = startIndex + (endIndex - startIndex) / 2
        return when {
            x < list[mid].x -> searchIndex(list, startIndex, mid - 1, x)
            x > buyList[mid].x -> searchIndex(list, mid + 1, endIndex, x)
            else -> mid
        }

    }

    fun getDimension(@DimenRes dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }


    fun getFormatValue(f: Float): String {
        return String.format("%.${scale}f", f)
    }

}