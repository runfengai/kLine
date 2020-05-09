package com.github.kline

import com.github.klib.entity.DataHelper
import com.github.klib.entity.KEntity
import com.github.klib.interfaces.BaseKChartAdapter


class KAdapter : BaseKChartAdapter<KEntity> {
    private var list = mutableListOf<KEntity>()

    constructor() {}
    constructor(list: MutableList<KEntity>) {
        this.list.addAll(list)
    }

    fun notify(list: List<KEntity>?) {
        if (list != null && list.isNotEmpty()) {
            this.list.clear()
            this.list.addAll(list)
            DataHelper.getALL(this.list)
            notifyDataSetChanged()
        }
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): KEntity {
        return list[position]
    }

    override fun getDate(position: Int): String {
        return list[position].dateTime
    }

}