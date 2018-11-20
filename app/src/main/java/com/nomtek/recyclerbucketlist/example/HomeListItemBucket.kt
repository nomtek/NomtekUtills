package com.nomtek.recyclerbucketlist.example

import android.graphics.Color
import com.nomtek.libs.recyclerbucketlist.ListItemBucket
import java.util.*

class HomeListItemBucket : ListItemBucket<HomeListItem>() {

    init {
        putItems(COLORS_ITEM_TOP_BUCKET, Collections.emptyList())
        putItems(COUNTER_ITEM_BUCKET, CounterHomeListItem(0))
        val colors: IntArray = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN)
        putItems(COLORS_ITEM_BUCKET, colors.map { ColorHomeListItem(it) })
    }

    fun setFooterItem(onAddColorButtonClicked: () -> Unit,
                      onRemoveColorButtonClicked: () -> Unit) {
        putItems(FOOTER_ITEM_BUCKET, FooterHomeListItem(onAddColorButtonClicked, onRemoveColorButtonClicked))
    }

    fun removeTopColor() {
        val items = getItems(COLORS_ITEM_TOP_BUCKET) as MutableList<HomeListItem>?
        items?.let {
            if (it.isNotEmpty()) {
                it.removeAt(it.size - 1)
                decrementCounter()
            }
        }
    }

    fun addTopColor() {
        addItems(COLORS_ITEM_TOP_BUCKET, ColorHomeListItem(Color.YELLOW))
        incrementCounter()
    }

    private fun getCounterValue(): Int {
        val item = getFirstItem(COUNTER_ITEM_BUCKET) as CounterHomeListItem
        return item.counterValue
    }

    private fun incrementCounter() {
        setCounterValue(getCounterValue() + 1)
    }

    private fun decrementCounter() {
        setCounterValue(getCounterValue() - 1)
    }

    private fun setCounterValue(counterValue: Int) {
        putItems(COUNTER_ITEM_BUCKET, CounterHomeListItem(counterValue))
    }


    companion object {

        private const val COLORS_ITEM_TOP_BUCKET = 0

        private const val COUNTER_ITEM_BUCKET = 1

        private const val COLORS_ITEM_BUCKET = 2

        private const val FOOTER_ITEM_BUCKET = 3
    }
}