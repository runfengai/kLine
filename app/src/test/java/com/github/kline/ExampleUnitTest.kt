package com.github.kline

import com.github.klib.entity.KEntity
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

import kotlin.math.max
import kotlin.math.min

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect1() {
//        var h = 200f
//        var l = 100f
//        val list = ArrayList<KEntity>()
//        //200....213 214 215     100...113 114 115
//        var maxH = 0
//        var sumL = 0
        //(213-163）/(213-100)
        //(214-164）/(214-101)
        //(215-165）/(215-102)
//        calculateWR(list)
    }

    @Test
    fun addition_isCorrect() {
        var h = 200f
        var l = 100f
        val list = ArrayList<KEntity>()
        for (i in 0..27) {
            val close = (h + l) / 2
            list.add(KEntity(highest = h++, lowest = l++, close = close))
        }
        calculateWR(list)
    }

    private fun calculateWR(list: List<KEntity>) {
        val len = list.size
        var h14Max: Float
        var l14Min: Float

//        val index = 0
        for (i in 0 until len) {
            val kEntity = list[i]
            if (i >= 13) {
                h14Max = Float.MIN_VALUE
                l14Min = Float.MAX_VALUE
                for (j in i - 13..i) {
                    val jKEntity = list[j]
                    h14Max = max(h14Max, jKEntity.highest)
                    l14Min = min(l14Min, jKEntity.lowest)
                }
                kEntity.wr = (h14Max - kEntity.close) / (h14Max - l14Min) * 100
                println(kEntity.wr)
            }
        }

    }
}
