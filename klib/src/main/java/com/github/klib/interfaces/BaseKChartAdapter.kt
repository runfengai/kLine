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
    override fun notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        }
    }

    /**
     *注册观察者
     */
    override fun registerDataSetObserver(dataSetObserver: DataSetObserver) {
        mDataSetObservable.registerObserver(dataSetObserver)
    }

    override fun unRegisterDataSetObserver(dataSetObserver: DataSetObserver) {
        mDataSetObservable.unregisterObserver(dataSetObserver)
    }

}