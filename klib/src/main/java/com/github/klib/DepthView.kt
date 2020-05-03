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
 * 深度图,启用
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
        mBottomPadding = getDimension(R.dimen.kline_padding).toInt()
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
            gridRows = getInteger(R.styleable.DepthView_gridRows, 5)
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
        calculate()
        invalidate()
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


        val drawHeight = mHeight - mTopWidth - mBottomPadding
        volumeSpace = drawHeight / volumeCount
        volumeMax = max(buyVolumeMax, sellVolumeMax)
        volumeItem = volumeMax / volumeCount //固定的

        //计算x,y坐标并赋值
        for (i in 0 until buyList.size) {
            val currX = getBuyXByPrice(buyList[i].price)
            val currY = getYByVolume(buyList[i].amount)
            buyList[i].x = currX
            buyList[i].y = currY
        }
        //计算x,y坐标并赋值
        for (i in 0 until sellList.size) {
            val currX = getBuyXByPrice(sellList[i].price)
            val currY = getYByVolume(sellList[i].amount)
            sellList[i].x = currX
            sellList[i].y = currY
        }
    }

    private var mWidth = 0
    private var mHeight = 0
    /**
     * 精度
     */
    var scale = 2
    /**
     * 顶部，需要留出位置显示买卖方向
     */
    var mTopWidth = 0f


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h


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
    private var volumeItem: Float = 0f
    private var volumeCount = 5//横坐标数量
    private var volumeSpace: Float = 0f


    /**
     * 绘制value
     */
    private fun drawValue(canvas: Canvas) {


        for (i in volumeCount downTo 1) {
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
            mHeight - mBottomPadding + textH,
            textPaint
        )
        val middleText = getFormatValue(priceMiddle)
        canvas.drawText(
            middleText,
            mWidth / 2 - textPaint.measureText(middleText) / 2,
            mHeight - mBottomPadding + textH,
            textPaint
        )
        val endText = getFormatValue(sellPriceMax)
        canvas.drawText(
            endText,
            mWidth - textPaint.measureText(endText) - topTextPadding.toFloat(),
            mHeight - mBottomPadding + textH,
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
     * 根据volume算出对应y值
     */
    private fun getYByVolume(volume: Float): Float {
        val drawHeight = mHeight - mTopWidth - mBottomPadding
        val scale = volume / volumeMax
        return mTopWidth + drawHeight * (1 - scale)
    }

    private fun getBuyXByPrice(price: Float): Float {
        val drawWidth = mWidth / 2
        val scale = (price - buyPriceMin) / (priceMiddle - buyPriceMin)
        return drawWidth * scale
    }

    /**
     * 根据volume算出对应y值
     */
//    private fun getSellYByVolume(amount: Float): Float {
//        val drawHeight = mHeight - mTopWidth - mBottomPadding
//        val scale = amount / volumeMax
//        return max(drawHeight * (1 - scale), mTopWidth)
//    }

    private fun getSellXByPrice(price: Float): Float {
        val drawWidth = mWidth / 2
        val scale = (price - priceMiddle) / (sellPriceMax - priceMiddle)
        return drawWidth * scale + mWidth / 2
    }


    /**
     * 绘制买区域
     */
    private fun drawLeft(canvas: Canvas) {
        for (i in 0 until buyList.size) {

            when (i) {
                0 -> {
                    buyPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )

                    buyFillPath.moveTo(
                        buyList[i].x
                        ,
                        (mHeight - mBottomPadding).toFloat()
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

                    buyPath.lineTo(mWidth / 2f, (mHeight - mBottomPadding).toFloat())

                    buyFillPath.lineTo(
                        buyList[i].x,
                        buyList[i].y
                    )

                    buyFillPath.lineTo(mWidth / 2f, (mHeight - mBottomPadding).toFloat())
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
                    sellPath.moveTo(mWidth / 2f, (mHeight - mBottomPadding).toFloat())

                    sellPath.lineTo(
                        sellList[i].x,
                        sellList[i].y
                    )

                    sellFillPath.moveTo(mWidth / 2f, (mHeight - mBottomPadding).toFloat())
//                    sellFillPath.lineTo(
//                        sellList[i].x,
//                        (mHeight - mBottomPadding - 0.5f)
//                    )
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
                    sellFillPath.lineTo(sellList[i].x, (mHeight - mBottomPadding).toFloat())
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
    private var mBottomPadding = 0
    //网格线
    private val mGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 绘制线
     */
    private fun drawGrid(canvas: Canvas) {
        if (gridRows > 0) {
            //        主图
            val widthF = mWidth.toFloat()
            val heightF = mHeight.toFloat()
            rowSpace =
                (heightF - mTopWidth - mBottomPadding) / gridRows

            for (i in 0..gridRows) {
                val startY = rowSpace * i + mTopWidth
                canvas.drawLine(0f, startY, widthF, startY, mGridPaint)
            }

            val columnSpace = widthF / gridColumns
            for (i in 1 until gridColumns) {
                val startX = columnSpace * i.toFloat()
                canvas.drawLine(startX, 0f, startX, heightF - mBottomPadding, mGridPaint)
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
    private var touchYOfLine = 0f

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
            x <= mWidth / 2 -> {
                isLeftTouch = true
                touchYOfLine = getLeftYByX(x)
            }
            else -> {
                isLeftTouch = false
                touchYOfLine = getRightYByX(x)
            }
        }
    }

    /**
     * 绘制选中区域
     */
    private fun drawSelected(canvas: Canvas) {
        if (inTouch or isLongPress) {
            val paint = if (isLeftTouch) buyPaint else sellPaint
//            val list = if (isLeftTouch) buyList else sellList
            canvas.drawCircle(highLightX, touchYOfLine, depthCircleRadius, paint)
        }
    }

    /**
     * 根据x坐标获取对应position
     */
    private fun getLeftYByX(x: Float): Float {
        val scale = x / (mWidth / 2)
        val price = scale * (priceMiddle - buyPriceMin) + buyPriceMin
        //二分查找
        if (price > buyList[buyList.size - 1].price) {//最后一个
            val lastX = buyList[buyList.size - 1].x
            val lastY = buyList[buyList.size - 1].y
            return (x - lastX) / (mWidth / 2 - lastX) * (mHeight - mBottomPadding - mTopWidth - lastY) + lastY
        }
        return searchVolumeIndex(price, buyList, x)
    }

    /**
     * 根据x坐标获取对应position
     */
    private fun getRightYByX(x: Float): Float {
        val scale = (x - mWidth / 2) / (mWidth / 2)
        val price = scale * (sellPriceMax - priceMiddle) + priceMiddle
        //
        return searchVolumeIndex(price, sellList, x)
    }

    /**
     * 二分查找 点对应的y
     */
    private fun searchVolumeIndex(price: Float, list: List<DepthEntity>, x: Float): Float {
        return searchVolumeIndex(price, list, 0, list.size - 1, x)
    }

    private fun searchVolumeIndex(
        price: Float,
        list: List<DepthEntity>,
        startIndex: Int,
        endIndex: Int, x: Float
    ): Float {
        if (endIndex == startIndex + 1) {

            return x / list[startIndex].x * list[startIndex].y

//            return (mWidth / 2 - x) / (mWidth / 2 - list[startIndex].x) * list[startIndex].amount
//            return list[startIndex].amount + (list[endIndex].amount - list[startIndex].amount) / 2
        } else if (startIndex == endIndex) {
            return list[startIndex].y
        }
        val mid = startIndex + (endIndex - startIndex) / 2
        return when {
            price < list[mid].price -> searchVolumeIndex(price, list, startIndex, mid - 1, x)
            price > buyList[mid].price -> searchVolumeIndex(price, list, mid + 1, endIndex, x)
            else -> list[mid].y
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