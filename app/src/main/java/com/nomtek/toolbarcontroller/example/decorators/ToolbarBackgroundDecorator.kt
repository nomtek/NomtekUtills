package com.nomtek.toolbarcontroller.example.decorators

import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import com.nomtek.libs.toolbarcontroller.ToolbarDecorator
import com.nomtek.toolbarcontroller.example.R

class ToolbarBackgroundColorDecorator constructor(@ColorRes private val colorRes: Int,
                                                  @DimenRes private val elevationRes: Int = R.dimen.default_toolbar_elevation) : ToolbarDecorator {

    override fun build(toolbar: Toolbar) {
        val elevation: Float = if (elevationRes == NO_SHADOW) 0f else toolbar.resources.getDimensionPixelSize(elevationRes).toFloat()
        toolbar.setBackgroundColor(ContextCompat.getColor(toolbar.context, colorRes))
        ViewCompat.setElevation(toolbar, elevation)
    }

    companion object {

        const val NO_SHADOW = -1

    }
}