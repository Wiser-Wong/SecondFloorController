package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment

class VpFragment: Fragment() {

    companion object {
        private const val PIC_ID = "picResId"
        fun newInstance(picResId: Int): VpFragment {
            val fragment = VpFragment()
            val bundle = Bundle()
            bundle.putInt(PIC_ID,picResId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.vp_fragment_item,container,false)
        view.findViewById<AppCompatImageView>(R.id.iv_banner).setImageResource(arguments?.getInt(
            PIC_ID)?:0)
        return view
    }

}