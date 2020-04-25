package com.github.klib

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.entity.KlineAttribute
import com.github.klib.interfaces.BaseKChartAdapter
import com.github.klib.interfaces.IAdapter
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

abstract class BaseKChartView : ScaleScrollView {
    companion object {
        //是否有副图
        const val TYPE_SUB = 0//有
        const val TYPE_NULL_SUB = -1//无副图
    }

    //属性大全
    var kLineAttribute = KlineAttribute()

    //宽高
    var mWidth = 0
        private set
    var mHeight = 0
        private set
    //各种padding
    var mTopPadding = 0
        private set
    var mBottomPadding = 0
        private set
    private var mVolumeTopPadding = 0
    private var mSubTopPadding = 0
    //子图最大
    private var mMainScaleY: Float = 1f

    private val mChildScaleY = 1f

    private val mChildChildScaleY = 1f
    //mb等值
    private var mChildMaxValue = Int.MAX_VALUE
    private var mChildMinValue = Int.MIN_VALUE

    //点的宽度
    private var mPointWidth = 6f
    //网格线
    private val mGridPaint = Paint(ANTI_ALIAS_FLAG)
    //文字
    private val mTextPaint = Paint(ANTI_ALIAS_FLAG)
    //背景
    private val mBackgroundPaint = Paint(ANTI_ALIAS_FLAG)
    //选中的k线高亮
    private val mSelectedLinePaint = Paint(ANTI_ALIAS_FLAG)

    var mSelectedIndex: Int = 0
        private set

    //适配器
    private var mAdapter: IAdapter<KEntity>? = null
    //个数
    private var mItemCount: Int = 0

    private var mDataLen = 0f
    //动画
    private var mAnimator: ValueAnimator? = null
    private val animationDuration = 500L
    //???支持超屏拖动
    private var mOverScrollRange = 40f

    private var onSelectedChangeListener: OnSelectedChangeListener? = null
    /**
     * 绘图相关
     */
    private var mChildDraw: IChartDraw<KEntity>? = null
    //用于绘制的
    protected lateinit var mMainView: IChartDraw<KEntity>
    //主图区域
    private var mMainRect: Rect = Rect()
    //成交量区域
    private var mVolumeRect: Rect = Rect()
    //副图区域（根据指标来的，可能没有）
    private var mSubRect: Rect = Rect()
    //副图类型
    private var type: Int = TYPE_NULL_SUB
    /**
     * volume图，内部可能包含kdj啥的
     */
    private val mVolumeDraws = mutableListOf<IChartDraw<KEntity>>()
    /**
     * 副图各种指标
     */
    private val mSubDraws = mutableListOf<IChartDraw<KEntity>>()
    /**
     *当前副图指标类型
     */
    private var mCurrSubDraw: IChartDraw<KEntity>? = null


    //float格式化
    val mValueFormatter: IValueFormatter = DefValueFormatter()
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
    ){
        init()
    }

    fun init() {
//        super.initDetector()
        val defPadding = getDimension(R.dimen.kline_padding).toInt()
        mTopPadding = defPadding
        mBottomPadding = defPadding
        mVolumeTopPadding = defPadding
        mSubTopPadding = defPadding
//        mAnimator = ValueAnimator.ofFloat(0f, 1f)
    }

    /**
     * 滑动距离
     */
    private var mTranslateX: Float = 0f

    fun notifyChanged() {
        if (mItemCount > 0) {
            mDataLen = mPointWidth * (mItemCount - 1)
            checkAndFixScrollX()
            setTranslateXFromScrollX(mScrollX)
        } else {
            scrollX = 0
        }
        invalidate()
        requestLayout()
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
        val showHeight = h - mTopPadding - mBottomPadding
        //默认有副图
        var mainH = (showHeight * 0.6f).toInt()
        var volumeH = (showHeight * 0.2f).toInt()
        val subH = (showHeight * 0.2f).toInt()
        if (type != TYPE_NULL_SUB) {//有副图
            mSubRect = Rect(0, mVolumeRect.bottom + mSubTopPadding, mWidth, mVolumeRect.bottom + subH)
        } else {
            mainH = (showHeight * 0.75f).toInt()
            volumeH = (showHeight * 0.25f).toInt()
        }
        mMainRect = Rect(0, mTopPadding, mWidth, mTopPadding + mainH)
        mVolumeRect =
            Rect(0, mMainRect.bottom + mVolumeTopPadding, mWidth, mMainRect.bottom + volumeH)
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
     * @param scrollX
     */
    private fun setTranslateXFromScrollX(scrollX: Int) {
        mTranslateX = scrollX + getMinTranslateX()
    }

    fun isFullScreen(): Boolean = mDataLen != 0f && mDataLen >= mWidth / mScaleX

    /**
     * 获取平移的最小值
     *
     * @return private float getMinTranslateX() {
     * return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
     * }
     */
    private fun getMinTranslateX() = -mDataLen + mWidth / mScaleX - mPointWidth / 2


    private fun getMaxTranslateX() = if (!isFullScreen()) {
        getMinTranslateX()
    } else {
        mPointWidth / 2
    }

    /**
     * 长按选中监听
     */

    interface OnSelectedChangeListener {
        fun onSelectedChanged(view: BaseKChartView, point: Any, index: Int)
    }

    /**
     * 各种指标画线
     */
    fun drawChildLine(
        canvas: Canvas,
        paint: Paint,
        startX: Float,
        startValue: Float,
        stopX: Float,
        stopValue: Float
    ) {
        canvas.drawLine(startX, getChildY(startValue), stopX, getChildY(stopValue), paint)
    }

    /**
     * 根据x,算出y
     */
    fun getChildY(value: Float): Float {
        return (mChildMaxValue - value) * mChildScaleY + mVolumeRect.top
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


    fun addChildDraw(item: IChartDraw<KEntity>) {
        mVolumeDraws.add(item)
    }

    fun addChildChildDraw(item: IChartDraw<KEntity>) {
        mSubDraws.add(item)
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
        if (type != TYPE_NULL_SUB) {
            this.mCurrSubDraw = mSubDraws[type]
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
    }

    /**
     * 计算值
     */
    private fun calculateValue() {
        if (!isLongPress) {
            mSelectedIndex = -1
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
            }
        }

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
            val endVal = getXByIndex(start)
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

    }

    private fun drawK(canvas: Canvas) {

    }

    private fun drawText(canvas: Canvas) {

    }

    /**
     * 根据指标值,获取
     */
    fun getMainY(value: Float): Float {
        return (mMainMaxVal - value) * mMainScaleY + mMainRect.top
    }


}