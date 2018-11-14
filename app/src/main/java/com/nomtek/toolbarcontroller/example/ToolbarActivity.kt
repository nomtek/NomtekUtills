package com.nomtek.toolbarcontroller.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nomtek.libs.toolbarcontroller.ToolbarController
import com.nomtek.toolbarcontroller.example.decorators.ToolbarBackgroundColorDecorator
import com.nomtek.toolbarcontroller.example.decorators.ToolbarTitleDecorator
import kotlinx.android.synthetic.main.activity_toolbar.*

class ToolbarActivity : AppCompatActivity() {

    private val toolbarController: ToolbarController = ToolbarController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbar)

        toolbarController
                .addDecorator(ToolbarBackgroundColorDecorator(colorRes= R.color.green))
                .addDecorator(ToolbarTitleDecorator(resources.getString(R.string.main_activity_toolbar_title)))
                .build(toolbar, R.layout.toolbar_activity_toolbar)


        goToNextActivityButton.setOnClickListener {
            startActivity(Intent(this, ToolbarSecondActivity::class.java))
        }

    }
}
