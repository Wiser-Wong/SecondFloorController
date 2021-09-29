package com.wiser.secondfloorcontroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wiser.secondfloor.SecondFloorController

class MainActivity : AppCompatActivity() {

    private var controller: SecondFloorController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ViewPropertyAnimator.animate(findViewById(R.id.btn)).y(100f).setDuration(2000).start()

        supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, MainFragment.newInstance(), MainFragment::javaClass.name).commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if (controller?.getCurrentItemIndex() == 1) {
            controller?.setCurrentItem(0)
        } else {
            super.onBackPressed()
        }
    }
}