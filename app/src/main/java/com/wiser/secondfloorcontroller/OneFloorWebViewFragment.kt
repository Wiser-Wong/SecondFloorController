package com.wiser.secondfloorcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment

class OneFloorWebViewFragment : Fragment() {

    companion object {
        fun newInstance(): OneFloorWebViewFragment {
            return OneFloorWebViewFragment()
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

        parentFragment()?.getController()?.addScrollListView(webView)

    }

    /**
     * 父Fragment
     */
    fun parentFragment(): SecondFloorFragment? {
        if (this@OneFloorWebViewFragment.parentFragment is SecondFloorFragment) {
            return (this@OneFloorWebViewFragment.parentFragment as SecondFloorFragment)
        }
        return null
    }
}
