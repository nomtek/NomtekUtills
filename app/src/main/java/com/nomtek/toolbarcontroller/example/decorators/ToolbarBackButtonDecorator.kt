package com.nomtek.toolbarcontroller.example.decorators

import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import com.nomtek.libs.toolbarcontroller.ToolbarDecorator
import com.nomtek.toolbarcontroller.example.R

class ToolbarBackButtonDecorator(private val onClickListener: View.OnClickListener) : ToolbarDecorator {

    override fun build(toolbar: Toolbar) {
        val backActionImageView : ImageView = toolbar.findViewById(R.id.backActionImageView)
        backActionImageView.setOnClickListener(onClickListener)
    }
}
