package com.github.klib.interfaces

import android.database.DataSetObserver

/**
 * 适配器
 */
interface IAdapter<T> {
    /**
     *count
     */
    fun getCount(): Int

    /**
     * item
     */
    fun getItem(position: Int): T

    /**
     * 日期
     */
    fun getDate(position: Int): String

    fun notifyDataSetChanged()

    fun registerDataSetObserver(dataSetObserver: DataSetObserver)

    fun unRegisterDataSetObserver(dataSetObserver: DataSetObserver)
}