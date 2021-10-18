package com.wiser.secondfloorcontroller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(private var fragments: MutableList<Fragment>?, manager: FragmentManager): FragmentPagerAdapter(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = fragments?.size?:0

    override fun getItem(position: Int): Fragment = fragments?.get(position)?:Fragment()
}