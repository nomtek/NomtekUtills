package com.nomtek.libs.statusbarcontroller

import android.app.Activity
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import com.nomtek.libs.R

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
data class StatusBarController(
        private val activity: Activity,
        private var backgroundColorArgb: Int? = null,
        private var backgroundColorRes: Int? = null,
        private var isDarkTint: Boolean = false
) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            create()
        }
    }

    private fun create() {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = obtainBackgroundColor()
        val statusBarAdditionalFlags = if (isDarkTint) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
        window.decorView.systemUiVisibility = statusBarAdditionalFlags
    }

    private fun obtainBackgroundColor(): Int {
        return if (isUsingDarkTintPreMarshamallow()) {
            ContextCompat.getColor(activity, R.color.nu_statusbarcontroler_gray)
        } else {
            if (backgroundColorArgb == null) {
                ContextCompat.getColor(activity, backgroundColorRes ?: R.color.colorPrimaryDark)
            } else {
                backgroundColorArgb!!
            }
        }
    }

    private fun isUsingDarkTintPreMarshamallow(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M && isDarkTint
    }
}