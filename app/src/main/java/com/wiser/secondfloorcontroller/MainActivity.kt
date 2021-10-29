package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.wiser.secondfloor.nineoldandroids.animation.ValueAnimator
import com.wiser.secondfloor.nineoldandroids.view.ViewHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ViewPropertyAnimator.animate(findViewById(R.id.fl_content)).y(-100f).setDuration(2000).start()
    }

    fun skipOneFloorHasSecondFloorRecyclerView(view: View) {
        OneFloorHasSecondFloorActivity.intent(this, SkipType.RECYCLERVIEW)
    }
    fun skipOneFloorHasSecondFloorScrollView(view: View) {
        OneFloorHasSecondFloorActivity.intent(this, SkipType.SCROLLVIEW)
    }
    fun skipOneFloorHasSecondFloorWebView(view: View) {
        OneFloorHasSecondFloorActivity.intent(this, SkipType.WEBVIEW)
    }

    fun skipOneFloorHasSecondFloorNoList(view: View) {
        OneFloorHasSecondFloorActivity.intent(this, SkipType.NOLIST)
    }

    fun skipOneFloorNoSecondFloorRecyclerView(view: View) {
        OneFloorNoSecondFloorActivity.intent(this, SkipType.RECYCLERVIEW)
    }
    fun skipOneFloorNoSecondFloorScrollView(view: View) {
        OneFloorNoSecondFloorActivity.intent(this, SkipType.SCROLLVIEW)
    }
    fun skipOneFloorNoSecondFloorWebView(view: View) {
        OneFloorNoSecondFloorActivity.intent(this, SkipType.WEBVIEW)
    }
    fun skipOneFloorNoSecondFloorNoList(view: View) {
        OneFloorNoSecondFloorActivity.intent(this, SkipType.NOLIST)
    }

}