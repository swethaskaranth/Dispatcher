package com.goflash.dispatch.util

class FixedSizeList<T>(
    private val max: Int,
    private val innerList: MutableList<T> = ArrayList<T>(),
) : MutableList<T> by innerList {

    override fun add(element: T): Boolean {
        if (innerList.size == max)
            innerList.removeAt(max - 1)
        innerList.add(0, element)
        return true
    }
}