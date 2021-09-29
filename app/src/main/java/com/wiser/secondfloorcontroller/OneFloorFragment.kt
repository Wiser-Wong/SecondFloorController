package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wiser.secondfloor.ScreenTools
import com.wiser.secondfloor.SecondFloorController

class OneFloorFragment : Fragment() {

    companion object {
        fun newInstance(): OneFloorFragment {
            return OneFloorFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.one_floor_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        val ivTop: AppCompatImageView? = view?.findViewById(R.id.iv_top)
        val tipView = view?.findViewById<TextView>(R.id.tv_pull_tip)

        val recyclerView: RecyclerView? = view?.findViewById(R.id.rlv_one_floor)
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OneFloorAdapter()
            parentFragment()?.getController()?.addRecyclerView(this)
        }

        parentFragment()?.getController()?.addOnPullScrollListener(object :
            SecondFloorController.OnPullScrollListener {
            override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                println("-------alpha------>>${scrollY}")
                if (parentFragment()?.getController()
                        ?.getCurrentRefreshStatus() != SecondFloorController.REFRESH_HEADER_RUNNING
                ) {
                    ivTop?.alpha =
                        1 - scrollY / ivTop?.measuredHeight!!
                }
            }
        })

//        parentFragment()?.getController()?.addHeaderHeight(tipView)
//        parentFragment()?.getController()
//            ?.addOnPullRefreshListener(object : SecondFloorController.OnPullRefreshListener {
//                override fun onPullStatus(status: Int) {
//                    when (status) {
//                        SecondFloorController.REFRESH_HEADER_PREPARE -> {
//                            tipView?.text = "下拉刷新"
//                        }
//                        SecondFloorController.REFRESH_HEADER_RUNNING -> {
//                            tipView?.text = "刷新中"
//                            parentFragment()?.getController()?.postDelayed(Runnable {
//                                parentFragment()?.getController()?.setRefreshComplete()
//                            }, 1500)
//                        }
//                        SecondFloorController.REFRESH_HEADER_END -> {
//                            Toast.makeText(activity, "刷新数据了", Toast.LENGTH_SHORT).show()
//                        }
//                        SecondFloorController.REFRESH_HEADER_TWO_FLOOR_PREPARE -> {
//                            tipView?.text = "继续下拉有惊喜哦"
//                        }
//                        SecondFloorController.REFRESH_HEADER_TWO_FLOOR_RUNNING -> {
//                            tipView?.text = "松手得惊喜"
//                        }
//                        SecondFloorController.PULL_ONE_FLOOR -> {
//                            tipView?.visibility = View.VISIBLE
//                            ivTop?.visibility = View.VISIBLE
//                        }
//                        SecondFloorController.PULL_SECOND_FLOOR -> {
//                            tipView?.visibility = View.INVISIBLE
//                            ivTop?.visibility = View.INVISIBLE
//                        }
//                    }
//                }
//            })

        parentFragment()?.getController()?.setOverlapDistance(ivTop)
    }

    /**
     * 父Fragment
     */
    fun parentFragment(): MainFragment? {
        if (this@OneFloorFragment.parentFragment is MainFragment) {
            return (this@OneFloorFragment.parentFragment as MainFragment)
        }
        return null
    }
}

class OneFloorAdapter : RecyclerView.Adapter<OneFloorAdapter.OneFloorHolder>() {


    class OneFloorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneFloorHolder {
        return OneFloorHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.one_floor_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OneFloorHolder, position: Int) {

    }

    override fun getItemCount(): Int = 50
}