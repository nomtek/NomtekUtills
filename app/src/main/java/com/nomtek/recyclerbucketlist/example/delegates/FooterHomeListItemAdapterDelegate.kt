package com.nomtek.recyclerbucketlist.example.delegates

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.nomtek.recyclerbucketlist.example.FooterHomeListItem
import com.nomtek.recyclerbucketlist.example.HomeListItem
import com.nomtek.toolbarcontroller.example.R
import kotlinx.android.synthetic.main.footer_list_item_view.view.*

class FooterHomeListItemAdapterDelegate : AbsListItemAdapterDelegate<FooterHomeListItem, HomeListItem,
        FooterHomeListItemAdapterDelegate.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_list_item_view,
                parent, false)
        return ViewHolder(view)
    }

    override fun isForViewType(item: HomeListItem, items: MutableList<HomeListItem>, position: Int): Boolean {
        return item is FooterHomeListItem
    }

    override fun onBindViewHolder(item: FooterHomeListItem, vh: ViewHolder, payload: MutableList<Any>) {
        vh.itemView.buttonAddColor.setOnClickListener {
            item.onAddColorButtonClicked()
        }
        vh.itemView.buttonRemoveColor.setOnClickListener {
            item.onRemoveColorButtonClicked()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}