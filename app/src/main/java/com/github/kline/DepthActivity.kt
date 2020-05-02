package com.github.kline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.klib.DepthView
import com.github.klib.entity.DepthEntity

class DepthActivity : AppCompatActivity() {

    private lateinit var depthView: DepthView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depth)
        depthView = findViewById(R.id.depthView)
        val buyList = mutableListOf<DepthEntity>()
        for (i in 0..10) {
            val item = DepthEntity()
            item.price = i * 10 + 0.55f
            item.volume = if (i % 2 == 0) 100f else 50f
            buyList.add(item)
        }
        val sellList = mutableListOf<DepthEntity>()
        for (i in 11..20) {
            val item = DepthEntity()
            item.price = i * 10 + 0.55f
            item.volume = if (i % 2 == 0) 100f else 50f
            sellList.add(item)
        }

        depthView.setData(buyList, sellList)
    }
}
