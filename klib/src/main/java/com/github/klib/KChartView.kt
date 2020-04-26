package com.github.klib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import com.github.klib.draw.*
import com.github.klib.util.DensityUtil

class KChartView : BaseKChartView {

    private lateinit var mProgressbar: ProgressBar
    private var isRefreshing = false
//    private var isLoadMoreEnd = false

    private var defM5Color: Int = 0
    private var defM10Color: Int = 0
    private var defM30Color: Int = 0

    /**
     * 绘图
     */
    private lateinit var mBollView: BollView
    private lateinit var mKdjView: KDJView
    private lateinit var mMacdView: MACDView

    private lateinit var mRsiView: RSIView
    private lateinit var mVolumeView: VolumnView
    /**
     * 允许滑动、缩放
     */
    //记录上次是否可以
    private var mLastScrollEnable = true
    private var mLastScaleEnable = true

    private var mLastType = TYPE_NULL_SUB


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attr,
        defStyle
    ) {
        initDefValue()
        getAttrs(attr)
        initView()
    }

    /**
     *
     */
    private fun initDefValue() {
        defM5Color = getColor(R.color.kline_ma5)
        defM10Color = getColor(R.color.kline_ma10)
        defM30Color = getColor(R.color.kline_ma30)
    }


    /**
     * 初始化控件
     */
    private fun initView() {
        mProgressbar = ProgressBar(context)
        val size = DensityUtil.dip2px(context, 50f)
        val lp = LayoutParams(size, size)
        lp.addRule(CENTER_IN_PARENT)
        addView(mProgressbar, lp)

        mBollView = BollView(this)
        mKdjView = KDJView(this)
        mMacdView = MACDView(this)
        mMainView = MainView(this)
        mRsiView = RSIView(this)
        mVolumeView = VolumnView(this)

        addVolumeDraw(mVolumeView)

        addSubDraw(mMacdView)
        addSubDraw(mKdjView)
        addSubDraw(mRsiView)


    }

    /**
     * 获取属性内容
     */
    private fun getAttrs(attr0: AttributeSet?) {
        val attr = attr0 ?: return
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.KChartView)
        klineAttribute.pointWidth =
            typedArray.getDimension(R.styleable.KChartView_pointWidth, getDimension(R.dimen.kline_point_width))
        klineAttribute.textSize =
            typedArray.getDimension(R.styleable.KChartView_textSize, getDimension(R.dimen.kline_text_size))
        klineAttribute.textColor =
            typedArray.getColor(R.styleable.KChartView_textColor, getColor(R.color.kline_text_color))
        klineAttribute.lineWidth =
            typedArray.getDimension(R.styleable.KChartView_lineWidth, getDimension(R.dimen.kline_line_width))
        klineAttribute.backgroundColor =
            typedArray.getColor(R.styleable.KChartView_backgroundColor, getColor(R.color.kline_background))
        klineAttribute.selectedLineColor =
            typedArray.getColor(R.styleable.KChartView_selectedLineColor, getColor(R.color.kline_text_color))
        klineAttribute.selectedLineWidth = typedArray.getDimension(
            R.styleable.KChartView_selectedLineWidth,
            getDimension(R.dimen.kline_selected_line_width)
        )
        klineAttribute.gridLineWidth =
            typedArray.getDimension(R.styleable.KChartView_gridLineWidth, getDimension(R.dimen.kline_grid_line_width))
        klineAttribute.gridLineColor =
            typedArray.getColor(R.styleable.KChartView_gridLineColor, getColor(R.color.kline_grid_line))
        //main主图
        klineAttribute.ma5Color = typedArray.getColor(R.styleable.KChartView_ma5Color, defM5Color)
        klineAttribute.ma10Color = typedArray.getColor(R.styleable.KChartView_ma10Color, defM10Color)
        klineAttribute.ma30Color = typedArray.getColor(R.styleable.KChartView_ma30Color, defM30Color)
        klineAttribute.candleWidth =
            typedArray.getDimension(R.styleable.KChartView_candleWidth, getDimension(R.dimen.kline_candle_width))
        klineAttribute.candleLineWidth = typedArray.getDimension(
            R.styleable.KChartView_candleLineWidth,
            getDimension(R.dimen.kline_candle_line_width)
        )
        klineAttribute.candleUpColor=typedArray.getColor(R.styleable.KChartView_candleUpColor,getColor(R.color.kline_candle_up_color))
        klineAttribute.candleDownColor=typedArray.getColor(R.styleable.KChartView_candleDownColor,getColor(R.color.kline_candle_down_color))
        klineAttribute.selectorBackgroundColor =
            typedArray.getColor(R.styleable.KChartView_selectorBackgroundColor, getColor(R.color.kline_selector))
        klineAttribute.selectorTextSize = typedArray.getDimension(
            R.styleable.KChartView_selectorTextSize,
            getDimension(R.dimen.kline_selected_line_width)
        )
        klineAttribute.candleSolid = typedArray.getBoolean(R.styleable.KChartView_candleSolid, true)


        /**
         * macd
         */
        //这个不该设置，需要和candleWidth一致
        klineAttribute.macdWidth =
            typedArray.getDimension(R.styleable.KChartView_macdWidth, klineAttribute.candleWidth)//注意顺序
        klineAttribute.difColor = typedArray.getColor(R.styleable.KChartView_difColor, defM5Color)
        klineAttribute.deaColor = typedArray.getColor(R.styleable.KChartView_deaColor, defM10Color)
        klineAttribute.macdColor = typedArray.getColor(R.styleable.KChartView_macdColor, defM30Color)
        /**
         * kdj
         */
        klineAttribute.kColor = typedArray.getColor(R.styleable.KChartView_kColor, defM5Color)
        klineAttribute.dColor = typedArray.getColor(R.styleable.KChartView_dColor, defM10Color)
        klineAttribute.jColor = typedArray.getColor(R.styleable.KChartView_jColor, defM30Color)
        /**
         * rsi
         */
        klineAttribute.rsi1Color = typedArray.getColor(R.styleable.KChartView_rsi1Color, defM5Color)
        klineAttribute.rsi2Color = typedArray.getColor(R.styleable.KChartView_rsi2Color, defM10Color)
        klineAttribute.rsi3Color = typedArray.getColor(R.styleable.KChartView_rsi3Color, defM30Color)
        /**
         * boll
         */
        klineAttribute.upColor = typedArray.getColor(R.styleable.KChartView_upColor, defM5Color)
        klineAttribute.mbColor = typedArray.getColor(R.styleable.KChartView_mbColor, defM10Color)
        klineAttribute.dnColor = typedArray.getColor(R.styleable.KChartView_dnColor, defM30Color)

        typedArray.recycle()
        updateKlineAttr()
    }


    override fun onRightSide() {

    }

    //预留回调接口，以后可以加加载更多
    override fun onLeftSide() {

    }

    /**
     * 等待对话框
     */
    fun showLoading() {
        if (!isRefreshing) {
            isRefreshing = true
            mProgressbar.visibility = View.VISIBLE

            //记录上一次是否可以滑动、缩放
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable

            isScrollEnable = false
            isScaleEnable = false
        }
    }

    /**
     * 隐藏加载框
     */
    fun hideLoading() {
        mProgressbar.visibility = View.GONE
        isScaleEnable = mLastScaleEnable
        isScrollEnable = mLastScrollEnable
    }

    fun refreshComplete() {
        isRefreshing = false
        hideLoading()
    }

    /**
     * 设置副图类型
     * ref  BaseKChartView.TYPE_NULL_SUB
     */
    fun setChildType(childType: Int) {
        setSubDraw(childType)
        //与上次类型一致
        if (childType * mLastType >= 0) {
            notifyChanged()
        } else {
            updateSize(mWidth, mHeight)
        }
        mLastType = childType
    }


}