package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wiser.secondfloor.ScreenTools
import com.wiser.secondfloor.SecondFloorController
import com.wiser.secondfloor.nineoldandroids.animation.ValueAnimator
import com.wiser.secondfloor.nineoldandroids.view.ViewHelper

class MainFragment: Fragment() {

    private var controller: SecondFloorController? = null

    companion object {
        fun newInstance(): MainFragment{
            return MainFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        controller = view?.findViewById(R.id.controller)
        val oneView = LayoutInflater.from(activity).inflate(R.layout.main_one_floor_layout, controller, false)
        val twoView = LayoutInflater.from(activity).inflate(R.layout.main_two_floor_layout, controller, false)
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_controller_one_floor, OneFloorFragment.newInstance(), OneFloorFragment::javaClass.name).commitAllowingStateLoss()
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_controller_two_floor, TwoFloorFragment.newInstance(), TwoFloorFragment::javaClass.name).commitAllowingStateLoss()
        controller?.addOneFloorView(oneView)
        controller?.addTwoFloorView(twoView)

        val animator =
            ValueAnimator.ofFloat((-(ScreenTools.getScreenHeight(activity!!) - 500)).toFloat(), 0f)
        animator.duration = 20000
        animator.addUpdateListener {
            val value = it.animatedValue
            ViewHelper.setTranslationY(controller, value as Float)
        }
//        animator.start()

        val headerView = LayoutInflater.from(activity).inflate(R.layout.pull_header, controller, false)
        val tipView = headerView?.findViewById<TextView>(R.id.tv_pull_tip)
        controller?.addHeaderView(headerView)
        controller?.addOnPullRefreshListener(object: SecondFloorController.OnPullRefreshListener{
            override fun onPullStatus(status: Int) {
                when(status) {
                    SecondFloorController.REFRESH_HEADER_PREPARE -> {
                        tipView?.text = "下拉刷新"
                    }
                    SecondFloorController.REFRESH_HEADER_RUNNING -> {
                        tipView?.text = "刷新中"
                        controller?.postDelayed(Runnable {
                            controller?.setRefreshComplete()
                        },1500)
                    }
                    SecondFloorController.REFRESH_HEADER_END -> {
                        Toast.makeText(activity, "刷新数据了", Toast.LENGTH_SHORT).show()
                    }
                    SecondFloorController.REFRESH_HEADER_TWO_FLOOR_PREPARE -> {
                        tipView?.text = "继续下拉有惊喜哦"
                    }
                    SecondFloorController.REFRESH_HEADER_TWO_FLOOR_RUNNING -> {
                        tipView?.text = "松手得惊喜"
                    }
                }
            }
        })
        controller?.addOnPullScrollListener(object : SecondFloorController.OnPullScrollListener {

            override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                println("滑动监听--->>$scrollY")
            }
        })

        backPress(view)
    }

    /**
     * Fragment返回处理 主要处理在二楼按返回键直接返回到一楼
     */
    private fun backPress(view: View?) {
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (controller?.getCurrentItemIndex() == SecondFloorController.TWO_FLOOR_INDEX) {
                if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    backOneFloor()
                }
            } else {
                if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    activity?.finish()
                }
            }
            true
        }
    }

    fun getController(): SecondFloorController? = controller

    /**
     * 返回到一楼
     */
    fun backOneFloor(isScroll: Boolean = true) {
        controller?.setCurrentItem(SecondFloorController.ONE_FLOOR_INDEX, isScroll)
    }

}