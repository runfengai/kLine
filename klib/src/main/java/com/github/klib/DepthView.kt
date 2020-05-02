package com.github.klib

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DepthView : View {




    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, styleAttr: Int) : super(
        context,
        attrs,
        styleAttr
    ) {
        initView()
    }

    private fun initView() {

    }
}