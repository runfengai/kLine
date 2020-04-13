package com.github.klib.interfaces

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

    fun getDate(position: Int): String

}