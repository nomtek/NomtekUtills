package com.nomtek.toolbarcontroller.example.decorators

import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.nomtek.libs.toolbarcontroller.ToolbarDecorator
import com.nomtek.toolbarcontroller.example.R

class ToolbarTitleDecorator(private val title: String) : ToolbarDecorator {

    override fun build(toolbar: Toolbar) {
        val titleTextView : TextView = toolbar.findViewById(R.id.titleTextView)
        titleTextView.text = title
    }
}
