package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.wiser.secondfloor.ScreenTools
import com.wiser.secondfloor.SecondFloorOverController

class OneFloorHasSecondFloorRecyclerViewFragment : Fragment() {

    companion object {
        fun newInstance(): OneFloorHasSecondFloorRecyclerViewFragment {
            return OneFloorHasSecondFloorRecyclerViewFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.one_floor_recyclerview_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        val viewpager: ViewPager? = view?.findViewById(R.id.viewpager)
        val tipView = view?.findViewById<TextView>(R.id.tv_pull_tip)

        val recyclerView: RecyclerView? = view?.findViewById(R.id.rlv_one_floor)
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OneFloorAdapter()
        }
        parentFragment()?.getController()?.addScrollListView(recyclerView)

        val fragments: MutableList<Fragment> = mutableListOf()
        fragments.add(VpFragment.newInstance(R.mipmap.aa))
        fragments.add(VpFragment.newInstance(R.mipmap.bb))
        fragments.add(VpFragment.newInstance(R.mipmap.cc))
        viewpager?.adapter = ViewPagerAdapter(fragments,childFragmentManager)

        parentFragment()?.getController()?.addOnPullScrollListener(object :
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
        parentFragment()?.getController()
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
                            parentFragment()?.getController()?.postDelayed(Runnable {
                                parentFragment()?.getController()?.setRefreshComplete()
                            }, 1500)
                        }
                        SecondFloorOverController.REFRESH_HEADER_END -> {
                            tipView?.visibility = View.INVISIBLE
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
                    when (status) {
                        SecondFloorOverController.PULL_ONE_FLOOR_RUNNING -> {
                            tipView?.visibility = View.INVISIBLE
                            viewpager?.visibility = View.VISIBLE
                        }
                        SecondFloorOverController.PULL_SECOND_FLOOR -> {
                            tipView?.visibility = View.INVISIBLE
                            viewpager?.visibility = View.INVISIBLE
                        }
                    }
                }
            })

//        parentFragment()?.getController()?.setOverlapDistance(0)
    }

    /**
     * 父Fragment
     */
    fun parentFragment(): OneFloorHasSecondFloorFragment? {
        if (this@OneFloorHasSecondFloorRecyclerViewFragment.parentFragment is OneFloorHasSecondFloorFragment) {
            return (this@OneFloorHasSecondFloorRecyclerViewFragment.parentFragment as OneFloorHasSecondFloorFragment)
        }
        return null
    }
}
