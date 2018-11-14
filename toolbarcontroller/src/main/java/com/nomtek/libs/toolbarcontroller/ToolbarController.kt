package com.nomtek.libs.toolbarcontroller

import android.support.annotation.LayoutRes
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View

class ToolbarController {

    private val decorators = ArrayList<ToolbarDecorator>()

    fun addDecorator(decorator: ToolbarDecorator): ToolbarController {
        decorators.add(decorator)
        return this
    }

    fun addDecorators(decorators: List<ToolbarDecorator>): ToolbarController {
        this.decorators.addAll(decorators)
        return this
    }

    fun build(toolbar: Toolbar, @LayoutRes toolbarLayoutId: Int) {
        toolbar.visibility = View.VISIBLE
        toolbar.removeAllViews()
        LayoutInflater.from(toolbar.context).inflate(toolbarLayoutId, toolbar, true)
        for (decorator in decorators) {
            decorator.build(toolbar)
        }
        decorators.clear()
    }
}
