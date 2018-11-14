package com.nomtek.toolbarcontroller.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.nomtek.libs.toolbarcontroller.ToolbarController
import com.nomtek.toolbarcontroller.example.decorators.ToolbarBackButtonDecorator
import com.nomtek.toolbarcontroller.example.decorators.ToolbarBackgroundColorDecorator
import com.nomtek.toolbarcontroller.example.decorators.ToolbarTitleDecorator
import kotlinx.android.synthetic.main.activity_main.*

class SecondActivity : AppCompatActivity() {

    private val toolbarController: ToolbarController = ToolbarController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        toolbarController
                .addDecorator(ToolbarBackgroundColorDecorator(colorRes = R.color.colorPrimary))
                .addDecorator(ToolbarTitleDecorator(resources.getString(R.string.second_activity_toolbar_title)))
                .addDecorator(ToolbarBackButtonDecorator(View.OnClickListener {
                    onBackPressed()
                }))
                .build(toolbar as Toolbar, R.layout.second_activity_toolbar)
    }

}
