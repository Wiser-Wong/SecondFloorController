package com.wiser.secondfloorcontroller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SecondFloorActivity: AppCompatActivity()  {

    companion object {
        const val SKIP_TYPE = "skipType"
        fun intent(activity: AppCompatActivity?,skipType: SkipType?) {
            activity?.apply {
                val intent = Intent(this,SecondFloorActivity::class.java)
                intent.putExtra(SKIP_TYPE,skipType?.type)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_floor)
        when(intent?.getStringExtra(SKIP_TYPE)) {
            SkipType.RECYCLERVIEW.type -> {
                supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, SecondFloorFragment.newInstance(SkipType.RECYCLERVIEW), SecondFloorFragment::javaClass.name).commitAllowingStateLoss()
            }
            SkipType.SCROLLVIEW.type -> {
                supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, SecondFloorFragment.newInstance(SkipType.SCROLLVIEW), SecondFloorFragment::javaClass.name).commitAllowingStateLoss()
            }
            SkipType.WEBVIEW.type -> {
                supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, SecondFloorFragment.newInstance(SkipType.WEBVIEW), SecondFloorFragment::javaClass.name).commitAllowingStateLoss()
            }
            SkipType.NOLIST.type -> {
                supportFragmentManager.beginTransaction().replace(R.id.fl_main_controller, SecondFloorFragment.newInstance(SkipType.NOLIST), SecondFloorFragment::javaClass.name).commitAllowingStateLoss()
            }
        }
    }
}