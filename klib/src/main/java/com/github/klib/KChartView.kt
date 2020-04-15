package com.github.klib

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar

class KChartView : BaseKChartView {

    private lateinit var mProgressbar: ProgressBar
    private var isRefreshing = false


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attr,
        defStyle
    ) {
        val attr = attr ?: return

    }

    /**
     *
     */
    override fun init() {
        super.init()

    }

    override fun onRightSide() {

    }

    override fun onLeftSide() {

    }


}