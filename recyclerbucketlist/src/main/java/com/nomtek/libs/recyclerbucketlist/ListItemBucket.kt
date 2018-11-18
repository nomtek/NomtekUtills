package com.nomtek.libs.recyclerbucketlist

import android.support.annotation.NonNull
import java.util.*

/**
 * Created by Krzysztof Krol on 21/08/2018.
 */
open class ListItemBucket<T> {

    private val itemMap: MutableMap<Int, MutableList<T>>

    val items: List<T>
        get() = flattenCollection(itemMap.values)

    init {
        itemMap = TreeMap()
    }


    fun putItems(position: Int, vararg items: T) {
        putItems(position, Arrays.asList(*items))
    }

    fun putItems(position: Int, list: List<T>) {
        val items = ArrayList(list)
        itemMap[position] = items
    }

    fun addItems(position: Int, vararg items: T) {
        addItems(position, Arrays.asList(*items))
    }


    fun addItems(position: Int, list: List<T>) {
        if (itemMap[position] == null) {
            putItems(position, list)
        } else {
            itemMap[position]!!.addAll(list)
        }
    }

    fun getFirstItemPosition(@NonNull predicate: (T) -> Boolean): Int {
        val items = items
        for (i in items.indices) {
            if (predicate.invoke(items[i])) {
                return i
            }
        }
        return INVALID_POSITION
    }

    fun getLastItemPosition(@NonNull predicate: (T) -> Boolean): Int {
        val items = items
        for (i in items.indices.reversed()) {
            if (predicate.invoke(items[i])) {
                return i
            }
        }
        return INVALID_POSITION
    }

    fun getFirstItemKey(predicate: (T) -> Boolean): Int {
        itemMap.forEach { (key, itemsList) ->
            run {
                for (item in itemsList) {
                    if (predicate.invoke(item)) {
                        return key
                    }
                }
            }
        }
        return INVALID_POSITION
    }

    fun getItems(position: Int): List<T>? {
        return itemMap[position]
    }

    fun getFirstItem(position: Int): T? {
        val items = getItems(position)
        return if (items == null || items.isEmpty()) null else items[0]
    }

    fun remove(position: Int): MutableList<T>? {
        return itemMap.remove(position)
    }

    fun checkExistSection(sectionId: Int): Boolean {
        return itemMap.containsKey(sectionId) && !itemMap[sectionId]!!.isEmpty()
    }

    fun clearItems(position: Int) {
        itemMap.remove(position)
    }

    fun clearAllItems() {
        itemMap.clear()
    }

    private fun <T> flattenCollection(collectionOfLists: Collection<List<T>>): List<T> {
        val result = ArrayList<T>()
        for (list in collectionOfLists) {
            result.addAll(list)
        }
        return result
    }

    companion object {
        const val INVALID_POSITION = -1
    }
}
