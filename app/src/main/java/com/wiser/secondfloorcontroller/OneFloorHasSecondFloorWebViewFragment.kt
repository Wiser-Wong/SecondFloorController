package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.wiser.secondfloor.ScreenTools

class OneFloorHasSecondFloorWebViewFragment : Fragment() {

    companion object {
        fun newInstance(): OneFloorHasSecondFloorWebViewFragment {
            return OneFloorHasSecondFloorWebViewFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.one_floor_webview_fragment, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        val webView = view?.findViewById<WebView>(R.id.webView)
        val webSettings = webView?.getSettings();

        // 是否开启JS支持
        webSettings?.javaScriptEnabled = true
        webView?.loadUrl("https://www.baidu.com")

        parentFragment()?.getController()?.setFrictionValue(2.0f)
        parentFragment()?.getController()?.setPullRefreshMaxDistance(ScreenTools.dip2px(context,80f))
        parentFragment()?.getController()?.setContinuePullIntoTwoFloorDistance(ScreenTools.dip2px(context,150f))
        parentFragment()?.getController()?.addScrollListView(webView)

    }

    /**
     * 父Fragment
     */
    fun parentFragment(): OneFloorHasSecondFloorFragment? {
        if (this@OneFloorHasSecondFloorWebViewFragment.parentFragment is OneFloorHasSecondFloorFragment) {
            return (this@OneFloorHasSecondFloorWebViewFragment.parentFragment as OneFloorHasSecondFloorFragment)
        }
        return null
    }
}
