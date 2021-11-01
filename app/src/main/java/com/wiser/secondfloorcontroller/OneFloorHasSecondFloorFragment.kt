package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wiser.secondfloor.SecondFloorOverController

class OneFloorHasSecondFloorFragment : Fragment() {

    private var overController: SecondFloorOverController? = null

    companion object {
        const val SKIP_TYPE = "skipType"
        fun newInstance(skipType: SkipType): OneFloorHasSecondFloorFragment {
            val fragment = OneFloorHasSecondFloorFragment()
            val bundle = Bundle()
            bundle.putString(SKIP_TYPE, skipType.type)
            fragment.arguments = bundle
            return fragment
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
        overController = view?.findViewById(R.id.controller)
        val oneView = LayoutInflater.from(activity)
            .inflate(R.layout.main_one_floor_layout, overController, false)
        val twoView = LayoutInflater.from(activity)
            .inflate(R.layout.main_two_floor_layout, overController, false)
        when (arguments?.getString(SKIP_TYPE)) {
            SkipType.RECYCLERVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorHasSecondFloorRecyclerViewFragment.newInstance(),
                        OneFloorHasSecondFloorRecyclerViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.SCROLLVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorHasSecondFloorScrollViewFragment.newInstance(),
                        OneFloorHasSecondFloorScrollViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.WEBVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorHasSecondFloorWebViewFragment.newInstance(),
                        OneFloorHasSecondFloorWebViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.NOLIST.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorHasSecondFloorSimpleFragment.newInstance(),
                        OneFloorHasSecondFloorSimpleFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            else -> {
            }
        }
        childFragmentManager.beginTransaction()
            .replace(
                R.id.fl_controller_two_floor,
                TwoFloorFragment.newInstance(),
                TwoFloorFragment::javaClass.name
            ).commitAllowingStateLoss()
        overController?.addOneFloorView(oneView)
        overController?.addTwoFloorView(twoView)

        val headerView =
            LayoutInflater.from(activity).inflate(R.layout.pull_header, overController, false)
        val tipView = headerView?.findViewById<TextView>(R.id.tv_pull_tip)
        overController?.addHeaderView(headerView)
        overController?.addOnPullRefreshListener(object :
            SecondFloorOverController.OnPullRefreshListener {
            override fun onPullStatus(status: Int) {
                when (status) {
                    SecondFloorOverController.REFRESH_HEADER_PREPARE -> {
                        overController?.setHeaderVisible(true)
                        tipView?.text = "下拉刷新"
                    }
                    SecondFloorOverController.REFRESH_HEADER_RUNNING -> {
                        tipView?.text = "刷新中"
                        overController?.postDelayed(Runnable {
                            overController?.setRefreshComplete()
                        }, 1500)
                    }
                    SecondFloorOverController.REFRESH_HEADER_END -> {
                        overController?.setHeaderVisible(false)
                        activity?.apply {
                            Toast.makeText(this, "刷新数据了", Toast.LENGTH_SHORT).show()
                        }
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

            }
        })
        overController?.addOnPullScrollListener(object :
            SecondFloorOverController.OnPullScrollListener {

            override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                println("滑动监听--->>$scrollY")
            }
        })

//        overController?.setGuideAnim()

        backPress(view)
    }

    /**
     * Fragment返回处理 主要处理在二楼按返回键直接返回到一楼
     */
    private fun backPress(view: View?) {
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (overController?.getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) {
                if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    backOneFloor()
                }
            } else {
                if (overController?.getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX) {
                    if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                        overController?.setGuideAnim()
                        activity?.finish()
                    }
                }
            }
            true
        }
    }

    fun getController(): SecondFloorOverController? = overController

    /**
     * 返回到一楼
     */
    fun backOneFloor(isScroll: Boolean = true) {
        overController?.setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX, isScroll)
    }

}