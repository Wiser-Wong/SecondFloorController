package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wiser.secondfloor.OneFloorHeaderController
import com.wiser.secondfloor.SecondFloorOverController

class OneFloorNoSecondFloorFragment : Fragment() {

    private var headerController: OneFloorHeaderController? = null

    companion object {
        const val SKIP_TYPE = "skipType"
        fun newInstance(skipType: SkipType): OneFloorNoSecondFloorFragment {
            val fragment = OneFloorNoSecondFloorFragment()
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
        val view = inflater.inflate(R.layout.main_fragment1, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        headerController = view?.findViewById(R.id.controller)
        val oneView = LayoutInflater.from(activity)
            .inflate(R.layout.main_one_floor_layout, headerController, false)
        when (arguments?.getString(SKIP_TYPE)) {
            SkipType.RECYCLERVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorNoSecondFloorRecyclerViewFragment.newInstance(),
                        OneFloorNoSecondFloorRecyclerViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.SCROLLVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorNoSecondFloorScrollViewFragment.newInstance(),
                        OneFloorNoSecondFloorScrollViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.WEBVIEW.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorNoSecondFloorWebViewFragment.newInstance(),
                        OneFloorNoSecondFloorWebViewFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            SkipType.NOLIST.type -> {
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.fl_controller_one_floor,
                        OneFloorNoSecondFloorSimpleFragment.newInstance(),
                        OneFloorNoSecondFloorSimpleFragment::javaClass.name
                    ).commitAllowingStateLoss()
            }
            else -> {
            }
        }

        headerController?.addContentView(oneView)

        val headerView =
            LayoutInflater.from(activity).inflate(R.layout.pull_header, headerController, false)
        val tipView = headerView?.findViewById<TextView>(R.id.tv_pull_tip)
        headerController?.addHeaderView(headerView)
        headerController?.addOnPullRefreshListener(object :
            SecondFloorOverController.OnPullRefreshListener {
            override fun onPullStatus(status: Int) {
                when (status) {
                    SecondFloorOverController.REFRESH_HEADER_PREPARE -> {
                        tipView?.text = "下拉刷新"
                    }
                    SecondFloorOverController.REFRESH_HEADER_RUNNING -> {
                        tipView?.text = "刷新中"
                        headerController?.postDelayed(Runnable {
                            headerController?.setRefreshComplete()
                        }, 1500)
                    }
                    SecondFloorOverController.REFRESH_HEADER_END -> {
                        activity?.apply {
                            Toast.makeText(this, "刷新数据了", Toast.LENGTH_SHORT).show()
                        }
                    }
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE -> {
                        tipView?.text = "松开进行刷新"
                    }
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING -> {
                        tipView?.text = "松手得惊喜"
                    }
                }
            }

            override fun onPullFloorStatus(status: Int) {

            }
        })
        headerController?.addOnPullScrollListener(object :
            SecondFloorOverController.OnPullScrollListener {

            override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                println("滑动监听--->>$scrollY")
            }
        })

        headerController?.setGuideAnim()

        backPress(view)
    }

    /**
     * Fragment返回处理 主要处理在二楼按返回键直接返回到一楼
     */
    private fun backPress(view: View?) {
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { v, keyCode, event ->
            if (headerController?.getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) {
                if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    backOneFloor()
                }
            } else {
                if (headerController?.getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX) {
                    if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                        overController?.setGuideAnim()
                        activity?.finish()
                    }
                }
            }
            true
        }
    }

    fun getController(): OneFloorHeaderController? = headerController

    /**
     * 返回到一楼
     */
    fun backOneFloor(isScroll: Boolean = true) {
        headerController?.setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX, isScroll)
    }

}