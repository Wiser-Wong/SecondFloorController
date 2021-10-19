package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ViewPropertyAnimator.animate(findViewById(R.id.btn)).y(100f).setDuration(2000).start()
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

}