package com.nomtek.recyclerbucketlist.example.delegates

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.nomtek.recyclerbucketlist.example.ColorHomeListItem
import com.nomtek.recyclerbucketlist.example.HomeListItem
import com.nomtek.toolbarcontroller.example.R
import kotlinx.android.synthetic.main.color_list_item_view.view.*

class ColorHomeListItemAdapterDelegate : AbsListItemAdapterDelegate<ColorHomeListItem, HomeListItem,
        ColorHomeListItemAdapterDelegate.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.color_list_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun isForViewType(item: HomeListItem, items: MutableList<HomeListItem>, position: Int):
            Boolean {
        return item is ColorHomeListItem
    }

    override fun onBindViewHolder(item: ColorHomeListItem, vh: ViewHolder, payload: MutableList<Any>) {
        vh.itemView.contentView.setBackgroundColor(item.color)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}