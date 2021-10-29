package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class OneFloorHasSecondFloorSimpleFragment : Fragment() {

    companion object {
        fun newInstance(): OneFloorHasSecondFloorSimpleFragment {
            return OneFloorHasSecondFloorSimpleFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.one_floor_simple_fragment, container, false)
    }
}
