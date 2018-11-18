package com.nomtek.recyclerbucketlist.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.nomtek.toolbarcontroller.example.R
import kotlinx.android.synthetic.main.activity_recycler.*

class RecyclerListItemBucketActivity : AppCompatActivity() {

    private val adapter = HomeListAdapter()

    private val bucket = HomeListItemBucket()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        bucket.setFooterItem(
                onAddColorButtonClicked = {
                    bucket.addTopColor()
                    refreshItems()
                },
                onRemoveColorButtonClicked = {
                    bucket.removeTopColor()
                    refreshItems()
                })
        refreshItems()

    }

    private fun refreshItems() {
        setItems(bucket.items)
    }

    private fun setItems(items: List<HomeListItem>) {
        adapter.items = items.toMutableList()
        adapter.notifyDataSetChanged()
    }
}
