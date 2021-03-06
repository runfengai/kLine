package com.github.kline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.klib.DepthView
import com.github.klib.entity.Depth
import com.github.klib.entity.DepthEntity
import com.github.kline.entity.Book
import com.github.kline.entity.OrdersItem
import com.github.kline.entity.SocketSnapShot
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class DepthActivity : AppCompatActivity() {

    private lateinit var depthView: DepthView
    private lateinit var thirdView: com.github.klib.depth.DepthView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depth)
        depthView = findViewById(R.id.depthView)
        thirdView = findViewById(R.id.thirdView)
        fetchData0()
//        fetchData()
    }

    /**
     * 组装数据
     */
    private fun fetchData0() {
        val buyList = mutableListOf<DepthEntity>()
        val buyOrders = mutableListOf<OrdersItem>()
        val size = 20
        for (i in 0..size) {
            val item = DepthEntity()
            item.price = i * 1 + 0.55
            item.amount = 100 * Random.nextDouble()
            if (i == size) {
                item.amount = 1.0
            }
            buyList.add(item)
            buyOrders.add(OrdersItem(item.amount, item.price))
        }
        val sellList = mutableListOf<DepthEntity>()
        val sellOrders = mutableListOf<OrdersItem>()
        for (i in size..size * 2) {
            val item = DepthEntity()
            item.price = i * 1 + 0.65
            item.amount = 100 * Random.nextDouble()
            if (i == size) {
                item.amount = 2.0
            }
            sellList.add(item)
            sellOrders.add(OrdersItem(item.amount, item.price))
        }

        depthView.setData(buyList, sellList)
        thridViewShow(buyOrders, sellOrders)
    }


    /**
     * 模拟网络请求
     */
    private fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            val res = withContext(Dispatchers.IO) {
                val sb = StringBuilder()
                try {
                    val inputStream = InputStreamReader(resources.assets.open("depth.json"))
                    val bufferedReader = BufferedReader(inputStream)
                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        sb.append(line)
                        line = bufferedReader.readLine()
                    }
                } catch (e: Exception) {
                }

                val book = Gson().fromJson(sb.toString(), Book::class.java)
                val bData = book.data
                val buyOrders = mutableListOf<OrdersItem>()
                bData.buyOrders?.forEach {
                    if (it.isNotEmpty() && it.size == 2) {
                        buyOrders.add(OrdersItem(it[1], it[0]))
                    }
                }
                val sellOrders = mutableListOf<OrdersItem>()
                bData.sellOrders?.forEach {
                    if (it.isNotEmpty() && it.size == 2) {
                        sellOrders.add(OrdersItem(it[1], it[0]))
                    }
                }
                SocketSnapShot(
                    book.symbol,
                    book.data.price,
                    sellOrders,
                    buyOrders,
                    book.sequenceId
                )


            }

            depthView.setData(res.buyOrders.toList(), res.sellOrders.toList())

            thridViewShow(res.buyOrders, res.sellOrders)

        }
    }

    private fun thridViewShow(
        buyOrders: MutableList<OrdersItem>,
        sellOrders: MutableList<OrdersItem>
    ) {
        val buyList = mutableListOf<Depth>()
        buyOrders.forEach {
            buyList.add(Depth(it.price, it.amount, 0))
        }
        val sellList = mutableListOf<Depth>()
        sellOrders.forEach {
            sellList.add(Depth(it.price, it.amount, 1))
        }

        thirdView.resetAllData(buyList, sellList)
    }
}
