package com.github.kline

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.github.klib.KChartView
import com.github.klib.KlineConfig
import com.github.klib.entity.DefValueFormatter
import com.github.klib.entity.KEntity
import com.github.kline.GlobalConstant.TAG_AN_HOUR
import com.github.kline.entity.KData
import com.github.kline.entity.ResponseEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
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
    private lateinit var mainMa: CheckBox
    private lateinit var mainBoll: CheckBox

    private lateinit var subMACD: RadioButton
    private lateinit var subKDJ: RadioButton
    private lateinit var subRSI: RadioButton
    private lateinit var subWR: RadioButton
    private lateinit var subClear: RadioButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        kChartView = findViewById(R.id.kChartView)
        mainMa = findViewById(R.id.mainMa)
        mainBoll = findViewById(R.id.mainBoll)

        subMACD = findViewById(R.id.subMACD)
        subKDJ = findViewById(R.id.subKDJ)
        subRSI = findViewById(R.id.subRSI)
        subWR = findViewById(R.id.subWR)
        subClear = findViewById(R.id.subClear)

        listeners()
        fetchData()

        //设置精度
        kChartView?.setValueFormatter(DefValueFormatter(4))
    }


    private fun listeners() {
        mainMa.setOnCheckedChangeListener { _, isChecked ->
            kChartView?.showMaAndBoll(showMa = isChecked)
            if (isChecked) {
                mainBoll.isChecked = false
            }
        }
        mainBoll.setOnCheckedChangeListener { _, isChecked ->
            kChartView?.showMaAndBoll(showBoll = isChecked)
            if (isChecked) {
                mainMa.isChecked = false
            }
        }

        subRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                subMACD.id -> {
                    KlineConfig.TYPE_SUB_MACD
                }
                subKDJ.id -> {
                    KlineConfig.TYPE_SUB_KDJ
                }
                subRSI.id -> {
                    KlineConfig.TYPE_SUB_RSI
                }
                subWR.id -> {
                    KlineConfig.TYPE_SUB_WR
                }
                else -> {
                    KlineConfig.TYPE_NULL_SUB
                }
            }
            kChartView?.setChildType(type)
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


    private fun fetchData() {
        GlobalScope.launch(Dispatchers.Main) {
            val res = withContext(Dispatchers.IO) {
                val sb = StringBuilder()
                try {
                    //                    val inputStream = InputStreamReader(resources.assets.open("kdata_less.json"))
                    val inputStream = InputStreamReader(resources.assets.open("kdata_m.json"))
                    //                    val inputStream = InputStreamReader(resources.assets.open("kdata.json"))
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
}
