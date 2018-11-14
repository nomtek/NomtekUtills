package com.nomtek.libs.toolbarcontroller

import android.support.v7.widget.Toolbar

interface ToolbarDecorator {

    companion object {
        val TINT_COLOR_WHITE = 0x01
        val TINT_COLOR_BLACK = 0x02
    }

    fun build(toolbar: Toolbar)
}
