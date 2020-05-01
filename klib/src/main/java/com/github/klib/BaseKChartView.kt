package com.github.klib

import android.content.Context
import android.database.DataSetObserver
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.github.klib.KlineConfig.TYPE_NULL_SUB
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.entity.KlineAttribute
import com.github.klib.interfaces.BaseKChartAdapter
import com.github.klib.interfaces.IAdapter
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

abstract class BaseKChartView : ScaleScrollView {
    //宽高
    var mWidth = 0
        private set
    var mHeight = 0
        private set
    //各种padding
    var mTopPadding = 0
        private set
    private var mTopMargin = 60
    private var mBottomPadding = 0
    //左侧各指标text显示区域高度
    private var mVolumeTopPadding = 0

    private var mLeftPadding = 0
    private var mEndPadding = 0

    //子图最大
    private var mMainScaleY: Float = 1f

    private var mVolumeScaleY = 1f

    private var mSubScaleY = 1f
    //点的宽度
    private var mPointWidth = 10f
    //网格线
    private val mGridPaint = Paint(ANTI_ALIAS_FLAG)
    //文字
    private val mTextPaint = Paint(ANTI_ALIAS_FLAG)
    //背景
    private val mBackgroundPaint = Paint(ANTI_ALIAS_FLAG)
    /**
     * 选中的k线高亮
     */
    private val mSelectedRowLinePaint = Paint(ANTI_ALIAS_FLAG)
    private val mSelectedColumnLinePaint = Paint(ANTI_ALIAS_FLAG)//列渐变效果
    private val mSelectedCirclePaint = Paint(ANTI_ALIAS_FLAG)
    private var mSelectedCircle0Color = 0
    private var mSelectedCircle1Color = 0

    var mSelectedIndex: Int = 0
        private set

    //适配器
    private var mAdapter: IAdapter<KEntity>? = null
    //个数
    private var mItemCount: Int = 0

    private var mDataLen = 0f
    //???
    private var mOverScrollRange = 0f
    //用于绘制的
    protected lateinit var mMainView: IChartDraw<KEntity>
    //主图区域
    private var mMainRect: Rect = Rect()
    //成交量区域
    var mVolumeRect: Rect = Rect()
        private set
    //副图区域（根据指标来的，可能没有）
    private var mSubRect: Rect = Rect()
    //副图类型
    private var type: Int = TYPE_NULL_SUB
    /**
     * volume图 含ma5Volume,ma10Volume
     */
    private lateinit var mVolumeView: IChartDraw<KEntity>
    /**
     * 副图各种指标
     */
    private val mSubViews = mutableListOf<IChartDraw<KEntity>>()
    /**
     *当前副图指标类型
     */
    private var mCurrSubView: IChartDraw<KEntity>? = null


    //float格式化
    var mValueFormatter: IValueFormatter = DefValueFormatter()
    /**
     * 计算当前屏幕的开始、结束索引
     */
    private var mStartIndex = 0
    private var mStopIndex = 0

    private var mMainMaxVal = Float.MAX_VALUE
    private var mMainMinVal = Float.MIN_VALUE
    private var mVolumeMaxVal = Float.MAX_VALUE
    private var mVolumeMinVal = Float.MIN_VALUE
    private var mSubMaxVal = Float.MAX_VALUE
    private var mSubMinVal = Float.MIN_VALUE


    /**
     * grid线的行间距
     */
    var rowSpace: Float = 0f
        private set

    val klineAttribute = KlineAttribute()


    private val mDataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            mItemCount = mAdapter?.getCount() ?: 0
            notifyChanged()
        }

        override fun onInvalidated() {
            mItemCount = mAdapter?.getCount() ?: 0
            notifyChanged()
        }
    }


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attr,
        defStyle
    ) {
        init()
    }

    fun init() {
        mTextPaint.textSize = klineAttribute.textSize
//        super.initDetector()
        val defPadding = getDimension(R.dimen.kline_padding).toInt()
        mLeftPadding = getDimension(R.dimen.kline_text_start_padding).toInt()
        mEndPadding = getDimension(R.dimen.kline_text_end_padding).toInt()
        mTopPadding = defPadding
        mBottomPadding = defPadding

//        mAnimator = ValueAnimator.ofFloat(0f, 1f)

        mTopMargin = getDimension(R.dimen.kline_top_margin).toInt()

        mSelectedCircle0Color = getColor(R.color.kline_selector_circle_0_color)
        mSelectedCircle1Color = getColor(R.color.kline_selector_circle_1_color)
    }

    /**
     * 滑动距离
     */
    private var mTranslateX: Float = 0f

    fun notifyChanged() {
        if (mItemCount > 0) {
            mDataLen = mPointWidth * mItemCount
            checkAndFixScrollX()
            setTranslateXFromScrollX(mScrollX)
        } else {
            scrollX = 0
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initRect(w, h)

        setTranslateXFromScrollX(mScrollX)
    }

    /**
     *更新显示区域
     */
    fun updateSize(w: Int, h: Int) {
        initRect(w, h)
        setTranslateXFromScrollX(mScrollX)
        notifyChanged()
    }

    private fun getTextHeight(): Int {
        val metrics = mTextPaint.fontMetrics
        return (metrics.descent - metrics.ascent).toInt()
    }

    /**
     * 限定三块的区域所占高度的比例，以及尺寸：
     * （1）有副图
     * main:volume:副=3:1:1
     * （2）无副图
     * main:volume=3:1
     */
    private fun initRect(w: Int, h: Int) {
        mWidth = w
        mHeight = h

        if (mVolumeTopPadding == 0) {//通过paint算出来的
            mVolumeTopPadding = getTextHeight()
        }
        val metricsBottom = mTextPaint.fontMetrics.bottom.toInt()
        val showHeight = h - mTopPadding - mBottomPadding - mTopMargin
        //默认有副图
        var mainH = (showHeight * 0.6f).toInt()
        var volumeH = (showHeight * 0.2f).toInt()
        val subH = (showHeight * 0.2f).toInt()
        if (type != TYPE_NULL_SUB) {//有副图
            mMainRect = Rect(0, mTopPadding + mTopMargin, mWidth, mTopPadding + mTopMargin + mainH)
            mVolumeRect =
                Rect(
                    0,
                    mMainRect.bottom + mVolumeTopPadding + metricsBottom,
                    mWidth,
                    mMainRect.bottom + volumeH
                )
            mSubRect =
                Rect(
                    0,
                    mVolumeRect.bottom + mVolumeTopPadding + metricsBottom,
                    mWidth,
                    mVolumeRect.bottom + subH
                )
        } else {
            mainH = (showHeight * 0.8f).toInt()
            volumeH = (showHeight * 0.2f).toInt()
            mMainRect = Rect(0, mTopPadding + mTopMargin, mWidth, mTopPadding + mTopMargin + mainH)
            mVolumeRect =
                Rect(
                    0,
                    mMainRect.bottom + mVolumeTopPadding + metricsBottom,
                    mWidth,
                    mMainRect.bottom + volumeH
                )
        }

    }

    override fun onLongPress(e: MotionEvent?) {
        super.onLongPress(e)
        if (e == null) return
        //
        this.mSelectedIndex = indexOfTranslateX(xToTranslateX(e.x))
        if (this.mSelectedIndex < mStartIndex) this.mSelectedIndex = mStartIndex
        if (this.mSelectedIndex > mStopIndex) this.mSelectedIndex = mStopIndex
        invalidate()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        setTranslateXFromScrollX(mScrollX)
    }

    override fun onScaleChanged(mScaleX: Float, oldScale: Float) {
        checkAndFixScrollX()
        setTranslateXFromScrollX(mScrollX)
        super.onScaleChanged(mScaleX, oldScale)
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX 距离
     */
    private fun setTranslateXFromScrollX(scrollX: Int) {
        mTranslateX = scrollX + getMinTranslateX()
//        if (needReLocate && isFullScreen()) {
//            mScrollX = -width / 5
//            mTranslateX -= width / 5
//        }
    }

    private fun isFullScreen(): Boolean {
        return mDataLen != 0f && mDataLen >= mWidth / mScaleX - mWidth / 5//固定的
    }

    /**
     * 获取平移的最小值
     *
     * @return private float getMinTranslateX() {
     * return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
     * }
     */
    private fun getMinTranslateX() =
        -mDataLen + mWidth / mScaleX + mPointWidth / 2 - mWidth / 5 / mScaleX


    private fun getMaxTranslateX() = if (!isFullScreen()) {
        getMinTranslateX()
    } else {
        0f
    }

    /**
     * 设置value精度
     */
    fun setValueFormatter(valueFormatter: IValueFormatter) {
        this.mValueFormatter = valueFormatter
    }

    /**
     * 格式化值
     */
    fun formatValue(value: Float): String {
        val formatter = mValueFormatter
        return formatter.format(value)
    }

    /**
     *
     */
    fun getItem(position: Int): KEntity? {
        return mAdapter?.getItem(position)
    }

    /**
     *translateX转化为view中的x
     */
    fun translateXtoX(translateX: Float): Float {
        return (translateX + mTranslateX) * mScaleX
    }

    /**
     * 最小滚动长度
     */
    override fun getMinScrollX(): Int = -(mOverScrollRange / mScaleX).toInt()

    override fun getMaxScrollX(): Int = round(getMaxTranslateX() - getMinTranslateX()).toInt()

    //
    fun addVolumeDraw(item: IChartDraw<KEntity>) {
        mVolumeView = item
    }

    fun addSubDraw(item: IChartDraw<KEntity>) {
        mSubViews.add(item)
    }

    /**
     * 适配器
     */
    fun setAdapter(adapter: BaseKChartAdapter<KEntity>) {
        mAdapter?.apply {
            this.unRegisterDataSetObserver(mDataSetObserver)
        }
        mAdapter = adapter
        mAdapter?.apply {
            this.registerDataSetObserver(mDataSetObserver)
            mItemCount = this.getCount()
        }
        notifyChanged()
    }

    fun getAdapter() = mAdapter

    fun getDimension(@DimenRes dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    /**
     * 设置副图绘制的类型
     */
    fun setSubDraw(type: Int) {
        this.type = type
        //索引设置成1开始
        if (type != TYPE_NULL_SUB && type <= mSubViews.size) {
            this.mCurrSubView = mSubViews[type - 1]
        }
    }

    /**
     *绘制流程：
     * 1.绘制背景
     * 2.计算显示区域的最大值最小值
     * 3.绘制grid
     * 4.绘制k线区域
     * 5.绘制文字
     * 6.绘制选中的高亮位置
     */
    override fun onDraw(canvas0: Canvas?) {
        super.onDraw(canvas0)
        val canvas = canvas0 ?: return
        canvas.drawColor(mBackgroundPaint.color)
        if (mWidth == 0 || mItemCount == 0) return
        calculateValue()
        canvas.save()
        canvas.scale(1f, 1f)
        drawGrid(canvas)
        drawK(canvas)
        drawText(canvas)
        drawValue(canvas, if (isLongPress) this.mSelectedIndex else mStopIndex)
        canvas.restore()
    }

    /**
     * 三个图的各指标
     */
    private fun drawValue(canvas: Canvas, position: Int) {
        if (position in 0..mItemCount) {
            mMainView.drawText(canvas, position, mLeftPadding.toFloat(), mTopPadding.toFloat())
            mVolumeView.drawText(
                canvas,
                position,
                mLeftPadding.toFloat(),
                mMainRect.bottom.toFloat() + mVolumeTopPadding
            )
            if (type != TYPE_NULL_SUB) {
                mCurrSubView?.apply {
                    drawText(canvas, position, mLeftPadding.toFloat(), mSubRect.top.toFloat())
                }
            }
        }


    }

    /**
     * 计算值
     */
    private fun calculateValue() {
        if (!isLongPress) {
            this.mSelectedIndex = -1
        }
        //找到索引
        mStartIndex = indexOfTranslateX(xToTranslateX(0f))
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth.toFloat()))

        mMainMaxVal = Float.MIN_VALUE
        mVolumeMaxVal = Float.MIN_VALUE
        mSubMaxVal = Float.MIN_VALUE
        mMainMinVal = Float.MAX_VALUE
        mVolumeMinVal = Float.MAX_VALUE
        mSubMinVal = Float.MAX_VALUE

        for (i in mStartIndex..mStopIndex) {
            val item = getItem(i)
            item?.apply {
                mMainMaxVal = max(mMainMaxVal, mMainView.getMaxValue(this))
                mMainMinVal = min(mMainMinVal, mMainView.getMinValue(this))

                mVolumeMaxVal = max(mVolumeMaxVal, mVolumeView.getMaxValue(this))
                mVolumeMinVal = min(mVolumeMinVal, mVolumeView.getMinValue(this))
                mCurrSubView?.let {
                    mSubMaxVal = max(mSubMaxVal, it.getMaxValue(this))
                    mSubMinVal = min(mSubMinVal, it.getMinValue(this))
                }
            }
        }
        //主图
        if (mMainMaxVal > mMainMinVal) {
            val pdd = (mMainMaxVal - mMainMinVal) * 0.05f
            mMainMaxVal += pdd
            mMainMinVal -= pdd
        } else if (mMainMaxVal == mMainMinVal) {
            val pdd = mMainMaxVal * 0.05f
            mMainMaxVal += pdd
            mMainMinVal -= pdd
            if (mMainMaxVal == 0f) {
                mMainMaxVal = 1f
            }
        }
        //volume
        if (mVolumeMaxVal == mVolumeMinVal) {
            val pdd = mVolumeMaxVal * 0.05f
            mVolumeMaxVal += pdd
            mVolumeMinVal -= pdd
            if (mVolumeMaxVal == 0f) mVolumeMaxVal = 1f
        }

        if (type != TYPE_NULL_SUB) {//副图
            mCurrSubView?.let {
                if (mSubMaxVal == mSubMinVal) {
                    val pdd = mSubMaxVal * 0.05f
                    mSubMaxVal += pdd
                    mSubMinVal -= pdd
                    if (mSubMaxVal == 0f) mSubMaxVal = 1f
                }
                mSubScaleY = mSubRect.height() * 1f / (mSubMaxVal - mSubMinVal)
            }
        }
        mMainScaleY = mMainRect.height() * 1f / (mMainMaxVal - mMainMinVal)
        mVolumeScaleY = mVolumeRect.height() * 1f / (mVolumeMaxVal - mVolumeMinVal)

    }

    /**
     * 屏幕中的x转换成已经滑动了的x
     */
    private fun xToTranslateX(x: Float): Float {
        return x / scaleX - mTranslateX
    }

    private fun indexOfTranslateX(translateX: Float): Int {
        return indexOfTranslateX(translateX, 0, mItemCount - 1)
    }

    /**
     * 滑动区域的索引
     */
    private fun indexOfTranslateX(translateX: Float, start: Int, end: Int): Int {
        if (start >= end) return start
        if (end - start == 1) {
            val startVal = getXByIndex(start)
            val endVal = getXByIndex(end)
            return if (abs(translateX - startVal) < abs(translateX - endVal)) start else end
        }
        val mid = start + (end - start) / 2
        val midVal = getXByIndex(mid)
        if (translateX < midVal) {
            return indexOfTranslateX(translateX, start, mid)
        } else if (translateX > midVal) {
            return indexOfTranslateX(translateX, mid + 1, end)
        } else {
            return mid
        }
    }

    fun getXByIndex(index: Int): Float {
        return index * mPointWidth
    }


    /**
     * 绘制grid
     */
    private fun drawGrid(canvas: Canvas) {
//        主图
        val widthF = mWidth.toFloat()
        val heightF = mHeight.toFloat()
        rowSpace = (heightF - mTopPadding - mTopMargin - mBottomPadding) / klineAttribute.gridRows
        for (i in 0..klineAttribute.gridRows) {
            val startY = rowSpace * i + mMainRect.top.toFloat()
            canvas.drawLine(0f, startY, widthF, startY, mGridPaint)
        }

        val columnSpace = widthF / klineAttribute.gridColumns
        for (i in 1 until klineAttribute.gridColumns) {
            val startX = columnSpace * i.toFloat()
            canvas.drawLine(startX, 0f, startX, heightF - mBottomPadding, mGridPaint)
        }

    }

    private fun drawK(canvas: Canvas) {
        //保存之前的平移缩放
        canvas.save()
        if (!isFullScreen()) {
            mTranslateX = getInitialTranslateX()
        }
        canvas.translate(mTranslateX * mScaleX, 0f)
        canvas.scale(mScaleX, 1f)
        for (i in mStartIndex..mStopIndex) {
            val currentPoint = getItem(i) ?: return
            val currPointX = getXByIndex(i)
            val lastPoint = if (i == 0) currentPoint else getItem(i - 1) ?: return
            val lastPointX = if (i == 0) currPointX else getXByIndex(i - 1)
            mMainView.drawTranslated(lastPoint, currentPoint, lastPointX, currPointX, canvas, i)
            mVolumeView.drawTranslated(
                lastPoint,
                currentPoint,
                lastPointX,
                currPointX,
                canvas,
                i
            )
            if (type != TYPE_NULL_SUB) {
                mCurrSubView?.drawTranslated(
                    lastPoint,
                    currentPoint,
                    lastPointX,
                    currPointX,
                    canvas,
                    i
                )
            }
        }
        //长按
        if (isLongPress) {
            if (mSelectedIndex < 0) return
            val point = getItem(this.mSelectedIndex) ?: return
            val x = getXByIndex(this.mSelectedIndex)
            val y = getMainY(point.close)
            val gradient = LinearGradient(
                x,
                mTopPadding.toFloat(),
                x,
                (mHeight - mBottomPadding).toFloat(),
//                intArrayOf(startColor, endColor),
                intArrayOf(
                    getColor(R.color.kline_selector_line_column_color_half),
                    getColor(R.color.kline_selector_line_column_color),
                    getColor(R.color.kline_selector_line_column_color_half)
                ),
                null, Shader.TileMode.MIRROR
            )
            mSelectedColumnLinePaint.shader = gradient
            //渐变竖线
            canvas.drawLine(
                x,
                mTopPadding.toFloat(),
                x,
                (mHeight - mBottomPadding).toFloat(), mSelectedColumnLinePaint
            )
            //横线
            canvas.drawLine(
                -mTranslateX,
                y,
                -mTranslateX + mWidth / mScaleX,
                y,
                mSelectedRowLinePaint
            )
            //
            mSelectedCirclePaint.color = mSelectedCircle0Color
            val r1 = klineAttribute.candleWidth / 2
            canvas.drawOval(
                x - r1 / mScaleX,
                y - r1,
                x + r1 / mScaleX,
                y + r1,
                mSelectedCirclePaint
            )
            mSelectedCirclePaint.color = mSelectedCircle1Color
            val r2 = klineAttribute.candleWidth * 1.5f
            canvas.drawOval(
                x - r2 / mScaleX,
                y - r2,
                x + r2 / mScaleX,
                y + r2,
                mSelectedCirclePaint
            )
            if (type != TYPE_NULL_SUB) {
                canvas.drawLine(
                    x,
                    (mSubRect.top - mVolumeTopPadding).toFloat(),
                    x,
                    mSubRect.bottom.toFloat(),
                    mSelectedRowLinePaint
                )
            }

        }
        //还原平移缩放
        canvas.restore()

    }

    /**
     * 获取平移最小值
     */
    private fun getInitialTranslateX(): Float {
        if (mWidth != 0 && mStopIndex >= 0) {
            if (!isFullScreen()) {//左
                val count = mStopIndex - mStartIndex + 1
                if (count > 0) {//目的，显示不满屏，靠左显示
                    return mPointWidth / 2
                }
            }
        }
        return -mDataLen + mWidth / mScaleX + mPointWidth / 2
    }

    /**
     * 画右侧数值
     */
    private fun drawText(canvas: Canvas) {
        val metrics = mTextPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent
//        val pdd = metrics.bottom
        val baselineH = (textHeight - metrics.bottom - metrics.top) / 2
        val mainRow = max(
            if (type == TYPE_NULL_SUB) klineAttribute.gridRows
            else klineAttribute.gridRows - 1, 2
        )
        val betweenItem = (mMainMaxVal - mMainMinVal) / (mainRow - 1)
        val textWidth = mTextPaint.measureText(formatValue(mMainMaxVal))
        /**
         * ----------画主图数值----------
         */
        for (i in 0 until mainRow) {
            val text = formatValue(mMainMaxVal - i * betweenItem)
            canvas.drawText(
                text,
                mWidth - textWidth - mEndPadding,
                mMainRect.top - metrics.bottom + i * rowSpace,
                mTextPaint
            )
        }
        /**
         * ----------画Volume数值----------
         */
        val vol = mVolumeView.getValueFormatter().format(mVolumeMaxVal)
        canvas.drawText(
            vol,
            mWidth - mTextPaint.measureText(vol) - mEndPadding,
            mMainRect.bottom.toFloat() + textHeight,
            mTextPaint
        )

        /**
         * ----------画时间----------
         */
        val columnSpace = mWidth.toFloat() / klineAttribute.gridColumns

        val startX = getXByIndex(mStartIndex) - mPointWidth / 2
        val stopX = getXByIndex(mStopIndex) + mPointWidth / 2

        for (i in 1 until klineAttribute.gridColumns) {
            val x = columnSpace * i.toFloat()
            val translateX = xToTranslateX(x)

            if (translateX in startX..stopX) {
                val index = indexOfTranslateX(translateX)
                val item = getItem(index)
                item?.apply {
                    canvas.drawText(
                        this.dateTime,
                        x - mTextPaint.measureText(this.dateTime) / 2,
                        mHeight - mBottomPadding + baselineH, mTextPaint
                    )
                }
            }

        }

    }

    /**
     * 根据指标值,获取
     */
    fun getMainY(value: Float): Float {
        return (mMainMaxVal - value) * mMainScaleY + mMainRect.top
    }

    fun getVolumeY(value: Float): Float {
        return (mVolumeMaxVal - value) * mVolumeScaleY + mVolumeRect.top
    }

    fun getSubY(value: Float): Float {
        return (mSubMaxVal - value) * mSubScaleY + mSubRect.top
    }

    fun setGridRows(r: Int) {
        klineAttribute.gridRows = if (r < 1) {
            1
        } else r
    }

    fun setGridColumns(c: Int) {
        klineAttribute.gridColumns = if (c < 1) {
            1
        } else c
    }

    /**
     * 更新属性
     */
    fun updateKlineAttr() {
        mSelectedRowLinePaint.color = klineAttribute.selectedLineColor
        mSelectedRowLinePaint.strokeWidth = klineAttribute.selectedLineWidth
        mSelectedColumnLinePaint.strokeWidth = klineAttribute.candleWidth

        mTextPaint.color = klineAttribute.textColor
        mTextPaint.textSize = klineAttribute.textSize
        mBackgroundPaint.color = klineAttribute.backgroundColor
        mPointWidth = klineAttribute.pointWidth
        mGridPaint.color = klineAttribute.gridLineColor
        mGridPaint.strokeWidth = klineAttribute.gridLineWidth

    }


}