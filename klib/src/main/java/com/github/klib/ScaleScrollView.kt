package com.github.klib

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.OverScroller
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class ScaleScrollView : RelativeLayout, GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {


    constructor(context: Context) : super(context) {
        initDetector()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initDetector()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initDetector()
    }

    //手势检测
    private lateinit var detector: GestureDetectorCompat
    //缩放检测
    private lateinit var scaleDetector: ScaleGestureDetector
    //
    private lateinit var overScroller: OverScroller
    //在触摸中
    var inTouch: Boolean = false
    //长按类型
    var isLongPress: Boolean = false
    //允许scale
    var isScaleEnable: Boolean = true
    //允许scroll
    var isScrollEnable: Boolean = true
    //多指触控
    var multipleTouch: Boolean = false
    //x滚动位置
    protected var mScrollX: Int = 0
    //缩放比例
    protected var mScaleX: Float = 1f
    protected var scaleMin: Float = 0.4f
    protected var scaleMax: Float = 3f


    private fun initDetector() {
        setWillNotDraw(false)
        detector = GestureDetectorCompat(context, this)
        scaleDetector = ScaleGestureDetector(context, this)
        overScroller = OverScroller(context)
    }


    /**
     * 位移最小值
     */
    abstract fun getMinScrollX(): Int

    /**
     * 位移最大值
     */
    abstract fun getMaxScrollX(): Int

    /**
     * 右滑到头
     */
    abstract fun onRightSide()

    /**
     * 左滑到头
     */
    abstract fun onLeftSide()


    override fun setScrollX(scrollX: Int) {
        this.mScrollX = scrollX
        scrollTo(mScrollX, 0)
    }


    override fun scrollBy(x: Int, y: Int) {
        scrollTo(mScrollX - (x / mScaleX).roundToInt(), 0)
    }

    override fun scrollTo(x: Int, y: Int) {
        if (!isScrollEnable) {
            overScroller.forceFinished(true)
            return
        }
        val oldX = mScrollX
        mScrollX = x
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            onRightSide()
            overScroller.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            mScrollX = oldX
            onLeftSide()
            overScroller.forceFinished(true)
        }
        onScrollChanged(mScrollX, 0, oldX, 0)
        invalidate()
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            if (!inTouch) {
                scrollTo(overScroller.currX, 0)
            } else {
                overScroller.forceFinished(true)
            }
        }
    }


    /**
     *
     * ====================GestureDetectorCompat====================
     */

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onDown(e: MotionEvent?): Boolean = false

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (!inTouch && isScrollEnable) {
            val velocityS = velocityX / mScaleX
            overScroller.fling(
                mScrollX,
                0,
                if (velocityS.isNaN()) 0 else velocityS.roundToInt(),
                0,
                Int.MIN_VALUE,
                Int.MAX_VALUE,
                0,
                0
            )
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!isLongPress && !multipleTouch) {
            scrollBy(distanceX.roundToInt(), 0)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        isLongPress = true
    }

    /**
     *
     * ====================GestureDetectorCompat====================
     *
     */

    override fun getScaleX(): Float {
        return mScaleX
    }

    /**
     *缩放核心代码
     */
    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        if (!isScrollEnable) return false
        val oldScale = mScaleX
        mScaleX *= detector?.scaleFactor ?: 1f
        if (mScaleX < scaleMin) {
            mScaleX = scaleMin
        } else if (mScaleX > scaleMax) {
            mScaleX = scaleMax
        }
        onScaleChanged(mScaleX, oldScale)
        return true
    }

    open fun onScaleChanged(mScaleX: Float, oldScale: Float) {
        invalidate()
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean = true

    override fun onScaleEnd(detector: ScaleGestureDetector?) {

    }

    private var lastX = 0f
    private var lastY = 0f
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val event = ev ?: return super.dispatchTouchEvent(ev)
        val x = event.rawX
        val y = event.rawY
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(x - lastX)
                val deltaY = abs(y - lastY)
                if (deltaX > deltaY || isLongPress) {
                    parent.parent.requestDisallowInterceptTouchEvent(true)
                    return true
                } else {
                    parent.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val ev = event ?: return true
        val mask = MotionEvent.ACTION_MASK
        val x = event.rawX
        val y = event.rawY
        when (ev.action and mask) {
            MotionEvent.ACTION_DOWN -> {
                inTouch = true
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                if (ev.pointerCount == 1) {//单指点击
                    val deltaX = abs(x - lastX)
                    val deltaY = abs(y - lastY)
                    if (deltaX > deltaY || isLongPress) {
                        parent.parent.requestDisallowInterceptTouchEvent(true)
                        if (isLongPress) {
                            onLongPress(ev)
                        }
                    }else{
                        parent.parent.requestDisallowInterceptTouchEvent(false)
                    }
                } else {
                    parent.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isLongPress = false
                inTouch = false
                invalidate()
            }
        }
        multipleTouch = ev.pointerCount > 1
        this.detector.onTouchEvent(ev)
        this.scaleDetector.onTouchEvent(ev)
        return true
    }

    /**
     *检查修正滑动
     */
    protected fun checkAndFixScrollX() {
        Log.e(
            "kline",
            "checkAndFixScrollX  ====>mScrollX=$mScrollX   getMaxScrollX()=${getMaxScrollX()}"
        )
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            overScroller.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            overScroller.forceFinished(true)
        }
    }

}