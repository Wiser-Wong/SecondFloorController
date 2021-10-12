package com.wiser.secondfloorcontroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ViewPropertyAnimator.animate(findViewById(R.id.btn)).y(100f).setDuration(2000).start()

//        supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, MainFragment.newInstance(), MainFragment::javaClass.name).commitAllowingStateLoss()
        supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, TestFragment.newInstance(), TestFragment::javaClass.name).commitAllowingStateLoss()
    }

}