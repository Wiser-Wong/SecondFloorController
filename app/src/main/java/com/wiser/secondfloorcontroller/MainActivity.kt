package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.wiser.secondfloor.nineoldandroids.animation.ValueAnimator
import com.wiser.secondfloor.nineoldandroids.view.ViewHelper
import com.wiser.secondfloor.nineoldandroids.view.ViewPropertyAnimator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val animator =
//            ValueAnimator.ofFloat(0f, 200f)
//        animator.addUpdateListener {
//            val value: Float = it.animatedValue as Float
//            ViewHelper.setTranslationY(findViewById(R.id.fl_content), value)
//        }
//        animator.interpolator = DecelerateInterpolator()
//        animator.duration = 2000
//        animator.start()
//        ViewPropertyAnimator.animate(findViewById(R.id.fl_content)).y(-100f).setDuration(2000).start()
    }

    fun skipSecondFloorRecyclerView(view: View) {
        SecondFloorActivity.intent(this, SkipType.RECYCLERVIEW)
    }
    fun skipSecondFloorScrollView(view: View) {
        SecondFloorActivity.intent(this, SkipType.SCROLLVIEW)
    }
    fun skipSecondFloorWebView(view: View) {
        SecondFloorActivity.intent(this, SkipType.WEBVIEW)
    }

    fun skipSecondFloorNoList(view: View) {
        SecondFloorActivity.intent(this, SkipType.NOLIST)
    }

}