package com.github.kline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.klib.KChartView
import com.github.klib.entity.KEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var kChartView: KChartView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        kChartView = findViewById(R.id.kChartView)

        GlobalScope.launch(Dispatchers.Main) {
            val res = withContext(Dispatchers.IO) {
                val sb = StringBuilder()
                try {
                    val inputStream = InputStreamReader(resources.assets.open("kdata.json"))
                    val bufferedReader = BufferedReader(inputStream)
                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        sb.append(line)
                        line = bufferedReader.readLine()
                    }
                } catch (e: Exception) {
                }
                val res = Gson().fromJson<ResponseEntity<KData>>(
                    sb.toString(),
                    object : TypeToken<ResponseEntity<KData>>() {}.type
                )

                //数据解析
                val list = res?.data?.results
                val arr = JSONArray(list)
                kDataSuccess(arr, 1)
                res
            }
        }
    }

    var type = 1
    /**
     * 数据渲染
     */
    private suspend fun kDataSuccess(obj: JSONArray, type: Int) {
        val kData = DataParse()
        kData.parseKLine(obj, this.type)
        val kLineDatas = kData.kLineDatas
        if (kLineDatas.size > 0) {
            var kLineEntities = ArrayList<KEntity>()
            kLineEntities.clear()
            //刷新下
            for (i in kLineDatas.indices) {
                val lineEntity = KEntity()
                val kLineBean = kLineDatas.get(i)
                lineEntity.dateTime = kLineBean.date
                lineEntity.open = kLineBean.open
                lineEntity.close = kLineBean.close
                lineEntity.highest = kLineBean.high
                lineEntity.lowest = kLineBean.low
                lineEntity.volume = kLineBean.vol
                kLineEntities.add(lineEntity)
            }
            kLineEntities = kData.getKlineDataByType(kLineEntities, this.type)
            //
            withContext(Dispatchers.Main) {
                //                kLineEntities.value = kLineEntities
            }

        }
    }

}
