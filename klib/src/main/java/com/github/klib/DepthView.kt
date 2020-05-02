package com.github.klib

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.github.klib.entity.DepthEntity

class DepthView : View {


    private val buyList = mutableListOf<DepthEntity>()
    private val sellList = mutableListOf<DepthEntity>()

    private val buyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val buyPath = Path()
    private val sellPath = Path()

    private var buyColor: Int = 0
    private var buyFillColor: Int = 0
    private var sellyColor: Int = 0
    private var sellyFillColor: Int = 0
    private var lineWidth: Int = 0


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

    private fun initView(attributeSet: AttributeSet?) {
        attributeSet?.apply {
            setAttrs(this)
        }


    }

    private fun setAttrs(attributeSet: AttributeSet) {
        val attrs = context.obtainStyledAttributes(attributeSet, R.styleable.DepthView)
        attrs.apply {
            buyColor=getColor(R.styleable.DepthView_buyColor,getColor(R.color.kline_background))
        }
        attrs.recycle()
    }

    fun getDimension(@DimenRes dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
}