package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class TwoFloorFragment: Fragment() {

    companion object {
        fun newInstance(): TwoFloorFragment{
            return TwoFloorFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.two_floor_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        view?.findViewById<Button>(R.id.back_home)?.setOnClickListener{
            parentFragment()?.backOneFloor()
        }

    }

    /**
     * 父Fragment
     */
    fun parentFragment(): SecondFloorFragment? {
        if (this@TwoFloorFragment.parentFragment is SecondFloorFragment) {
            return (this@TwoFloorFragment.parentFragment as SecondFloorFragment)
        }
        return null
    }

}