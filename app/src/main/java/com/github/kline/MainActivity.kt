package com.github.kline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.klib.KChartView
import com.github.klib.entity.KEntity
import com.github.kline.GlobalConstant.TAG_AN_HOUR
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private var kChartView: KChartView? = null
    private var adapter = KAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        kChartView = findViewById(R.id.kChartView)

        GlobalScope.launch(Dispatchers.Main) {
            val res = withContext(Dispatchers.IO) {
                val sb = StringBuilder()
                try {
//                    val inputStream = InputStreamReader(resources.assets.open("kdata_less.json"))
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
                kDataSuccess(arr)
            }
            kChartView?.setAdapter(adapter)
            adapter.notify(res)
            kChartView?.refreshComplete()


        }
    }

    var type = TAG_AN_HOUR
    /**
     * 数据渲染
     */
    private fun kDataSuccess(obj: JSONArray): java.util.ArrayList<KEntity> {
        val kData = DataParse()
        kData.parseKLine(obj, this.type)
        return kData.kLineDatas

    }

}
