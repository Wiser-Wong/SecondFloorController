package com.wiser.secondfloor

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

class ScreenTools {

    companion object {
        /**
         * 获取屏幕高度
         *
         * @return
         */
        fun getScreenDPI(context: Context): Int {
            var dpi = 0
            val windowManager = (context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            val c: Class<*>
            try {
                c = Class.forName("android.view.Display")
                val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
                method.invoke(display, displayMetrics)
                dpi = displayMetrics.heightPixels
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dpi
        }

        /**
         * 获得屏幕的高度
         *
         * @return
         */
        fun getScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        /**
         * dip转换成px
         *
         * @param dpValue
         * @return
         */
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * px转换成dip
         *
         * @param pxValue
         * @return
         */
        fun px2dip(context: Context, pxValue: Float): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        /**
         * 将px值转换为sp值
         *
         * @param pxValue
         * @return
         */
        fun px2sp(context: Context, pxValue: Float): Int {
            val fontScale: Float =
                context.resources.displayMetrics.scaledDensity
            return (pxValue / fontScale + 0.5f).toInt()
        }

        /**
         * 将sp值转换为px值
         *
         * @param spValue
         * @return
         */
        fun sp2px(context: Context, spValue: Float): Int {
            val fontScale: Float =
                context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }
    }

}