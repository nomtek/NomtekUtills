package com.nomtek.centercropvideoview.example

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.nomtek.toolbarcontroller.example.R
import kotlinx.android.synthetic.main.activity_center_crop_video.*

class CenterCropVideoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_center_crop_video)
        val path = "android.resource://${this.packageName}/${R.raw.sample}"
        centerCropVideoView.setVideoURI(Uri.parse(path))
        centerCropVideoView.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            it.start()
        })

    }

    override fun onResume() {
        super.onResume()
        centerCropVideoView.start()
    }

    override fun onPause() {
        super.onPause()
        centerCropVideoView.pause()
    }
}
