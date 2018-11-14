package com.nomtek.statusbarcontroller.example

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nomtek.libs.statusbarcontroller.StatusBarController
import com.nomtek.toolbarcontroller.example.R
import kotlinx.android.synthetic.main.activity_statusbar.*

class StatusBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statusbar)
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        redButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StatusBarController(this, backgroundColorRes = R.color.red, isDarkTint = true)
            }
        }
        blackButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StatusBarController(this, backgroundColorRes = R.color.black, isDarkTint = false)
            }
        }
        defaultButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StatusBarController(this)
            }
        }
    }
}
