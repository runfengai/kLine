package com.github.klib.interfaces

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * k线数据适配器
 */
abstract class BaseKChartAdapter<T> : IAdapter<T> {
    val mDataSetObservable = DataSetObservable()
    /**
     *
     */
    fun notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        }
    }

    /**
     *注册观察者
     */
    fun registerDataSetObserver(dataSetObserver: DataSetObserver) {
        mDataSetObservable.registerObserver(dataSetObserver)
    }

    fun unRegisterDataSetObserver(dataSetObserver: DataSetObserver) {
        mDataSetObservable.unregisterObserver(dataSetObserver)

    }

}