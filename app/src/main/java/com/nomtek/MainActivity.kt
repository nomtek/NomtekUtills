package com.nomtek

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.nomtek.centercropvideoview.example.CenterCropVideoViewActivity
import com.nomtek.recyclerbucketlist.example.RecyclerListItemBucketActivity
import com.nomtek.statusbarcontroller.example.StatusBarActivity
import com.nomtek.toolbarcontroller.example.R
import com.nomtek.toolbarcontroller.example.ToolbarActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        toolbarButton.setOnClickListener {
            startActivity(Intent(this, ToolbarActivity::class.java))
        }
        statusbarButton.setOnClickListener {
            startActivity(Intent(this, StatusBarActivity::class.java))
        }
        recyclerListItemBucketButton.setOnClickListener {
            startActivity(Intent(this, RecyclerListItemBucketActivity::class.java))
        }
        centerCropVideoViewButton.setOnClickListener {
            startActivity(Intent(this, CenterCropVideoViewActivity::class.java))
        }
    }
}
