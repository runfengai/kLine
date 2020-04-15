package com.github.klib

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect
import android.util.AttributeSet
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.klib.entity.KlineAttribute
import com.github.klib.interfaces.IAdapter
import com.github.klib.interfaces.IChartDraw
import com.github.klib.interfaces.IValueFormatter
import kotlin.math.round

abstract class BaseKChartView : ScaleScrollView {
    companion object {
        //副图
        const val TYPE_SUB = 0
        const val TYPE_NULL_SUB = -1
    }

    //属性大全
    protected var kLineAttribute = KlineAttribute()

    //宽高
    private var mWidth = 0
    private var mHeight = 0
    //子图最大
    private var mChildScaleY = 1f
    //mb等值
    private var mChildMaxValue = Int.MAX_VALUE
    private var mChildMinValue = Int.MIN_VALUE

    //点的宽度
    private var mPointWidth = 6
    //网格线
    private val mGridPaint = Paint(ANTI_ALIAS_FLAG)
    //文字
    private val mTextPaint = Paint(ANTI_ALIAS_FLAG)
    //背景
    private val mBackgroundPain = Paint(ANTI_ALIAS_FLAG)
    //选中的k线高亮
    private val mSelectedLinePaint = Paint(ANTI_ALIAS_FLAG)

    private var mSelectedIndex: Int = 0

    //适配器
    private var mAdapter: IAdapter<KEntity>? = null
    //个数
    private var mItemCount: Int = 0

    private var mDataLen = 0
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
    private lateinit var mMainView: IChartDraw<KEntity>

    private var mMainRect: Rect = Rect()
    private var mChildRect: Rect = Rect()
    //todo
    private var mChildChildRect: Rect = Rect()
    //副图类型
    private var type: Int = TYPE_NULL_SUB



    //float格式化
    val mValueFormatter: IValueFormatter = DefValueFormatter()

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
    )

    override fun init() {
        super.init()
//        mAnimator = ValueAnimator.ofFloat(0f, 1f)
    }

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


    private fun initRect(w: Int, h: Int) {
        mWidth = w
        mHeight = h


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

    fun isFullScreen(): Boolean = mDataLen != 0 && mDataLen >= mWidth / mScaleX

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
        (mPointWidth / 2).toFloat()
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
        return (mChildMaxValue - value) * mChildScaleY + mChildRect.top
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
     * 最小滚动长度
     */
    override fun getMinScrollX(): Int = -(mOverScrollRange / mScaleX).toInt()

    override fun getMaxScrollX(): Int = round(getMaxTranslateX() - getMinTranslateX()).toInt()
}