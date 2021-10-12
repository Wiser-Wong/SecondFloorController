package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wiser.secondfloor.OneFloorController
import com.wiser.secondfloor.ScreenTools
import com.wiser.secondfloor.SecondFloorOverController

class TestFragment : Fragment() {

    private var controller: OneFloorController?= null

    companion object {
        fun newInstance(): TestFragment {
            return TestFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.test_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        val tipView = view?.findViewById<TextView>(R.id.tv_pull_tip)
        controller = view?.findViewById(R.id.controller)
        controller?.addOnPullScrollListener(object :
            SecondFloorOverController.OnPullScrollListener {
            override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                println("-------alpha------>>${scrollY}")
//                if (parentFragment()?.getController()
//                        ?.getCurrentRefreshStatus() != SecondFloorController.REFRESH_HEADER_RUNNING
//                ) {
//                    ivTop?.alpha =
//                        1 - scrollY / ivTop?.measuredHeight!!
//                }
                val distance: Int = activity?.let { ScreenTools.dip2px(it, 50f) } ?: 0
                tipView?.alpha = scrollY / distance
            }
        })

//        parentFragment()?.getController()?.addHeaderHeight(tipView)
        controller
            ?.addOnPullRefreshListener(object : SecondFloorOverController.OnPullRefreshListener {
                override fun onPullStatus(status: Int) {
                    println("=====>>状态：--$status")
                    when (status) {
                        SecondFloorOverController.REFRESH_HEADER_PREPARE -> {
                            tipView?.visibility = View.VISIBLE
                            tipView?.text = "下拉刷新"
                        }
                        SecondFloorOverController.REFRESH_HEADER_RUNNING -> {
                            tipView?.text = "刷新中"
                            controller?.postDelayed(Runnable {
                                controller?.setRefreshComplete()
                            }, 1500)
                        }
                        SecondFloorOverController.REFRESH_HEADER_END -> {
                            tipView?.visibility = View.INVISIBLE
                            Toast.makeText(activity, "刷新数据了", Toast.LENGTH_SHORT).show()
                        }
                        SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE -> {
                            tipView?.text = "继续下拉有惊喜哦"
                        }
                        SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING -> {
                            tipView?.text = "松手得惊喜"
                        }
                    }
                }

                override fun onPullFloorStatus(status: Int) {
                    when (status) {
                        SecondFloorOverController.PULL_ONE_FLOOR_RUNNING -> {
                            tipView?.visibility = View.INVISIBLE
                        }
                        SecondFloorOverController.PULL_SECOND_FLOOR -> {
                            tipView?.visibility = View.INVISIBLE
                        }
                    }
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
            if (controller?.getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) {
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

    /**
     * 返回到一楼
     */
    fun backOneFloor(isScroll: Boolean = true) {
        if (controller?.getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) {
            controller?.setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX, isScroll)
        }
    }
}