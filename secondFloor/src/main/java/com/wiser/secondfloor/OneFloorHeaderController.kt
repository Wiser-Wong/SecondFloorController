package com.wiser.secondfloor

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout

/**
 * @author Wiser
 *
 * 一楼触摸事件
 */
class OneFloorHeaderController(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {

    /**
     * 屏幕高度
     */
    private var screenHeight: Int = ScreenTools.getScreenHeight(context)

    /**
     * 当前位置
     */
    private var currentItemIndex: Int = SecondFloorOverController.ONE_FLOOR_INDEX

    /**
     * 摩擦力
     */
    private var frictionValue = 2f

    /**
     * 是否拦截一楼触摸事件
     */
    private var isInterceptOneFloorTouch = false

    /**
     * 是否刷新回弹
     */
    private var isRefreshingBackAnim = true

    /**
     * 是否禁止二楼
     */
    private var isNoSecondFloor = true

    /**
     * 初始化位置
     */
    private var initTranslationY = 0f

    /**
     * Header高度
     */
    private var headerHeight = 0

    /**
     * 下拉刷新最大距离
     */
    private var pullRefreshMaxDistance = ScreenTools.dip2px(context, 100f)

    /**
     * 继续下拉进入二楼距离
     */
    private var continuePullIntoTwoFloorDistance = ScreenTools.dip2px(context, 200f)

    private var oneFloorController: OneFloorController? = null

    init {
        val ta: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SecondFloorOverController)
        frictionValue =
            ta.getFloat(R.styleable.SecondFloorOverController_sfc_friction_value, frictionValue)
        currentItemIndex =
            ta.getInt(R.styleable.SecondFloorOverController_sfc_show_item, currentItemIndex)
        pullRefreshMaxDistance = ta.getDimension(
            R.styleable.SecondFloorOverController_sfc_pull_refresh_distance,
            pullRefreshMaxDistance.toFloat()
        ).toInt()
        headerHeight = ta.getDimension(
            R.styleable.SecondFloorOverController_sfc_header_height,
            headerHeight.toFloat()
        ).toInt()
        continuePullIntoTwoFloorDistance = ta.getDimension(
            R.styleable.SecondFloorOverController_sfc_pull_into_two_floor_distance,
            continuePullIntoTwoFloorDistance.toFloat()
        ).toInt()
        isInterceptOneFloorTouch = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_intercept_one_floor_touch,
            isInterceptOneFloorTouch
        )
        isRefreshingBackAnim = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_refreshing_back_anim,
            isRefreshingBackAnim
        )
        isRefreshingBackAnim = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_refreshing_back_anim,
            isRefreshingBackAnim
        )
        isNoSecondFloor = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_no_second_floor,
            isNoSecondFloor
        )
        ta.recycle()

        oneFloorController = OneFloorController(context, attrs)
        oneFloorController?.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        initTranslationY = -screenHeight.toFloat()

        addView(oneFloorController)

        initOneFloorHeaderController(
            screenHeight,
            currentItemIndex,
            frictionValue,
            isInterceptOneFloorTouch,
            isRefreshingBackAnim,
            initTranslationY,
            pullRefreshMaxDistance,
            continuePullIntoTwoFloorDistance,
            headerHeight,
            isNoSecondFloor
        )
    }

    fun initOneFloorHeaderController(
        screenHeight: Int,
        currentItemIndex: Int,
        frictionValue: Float,
        isInterceptOneFloorTouch: Boolean,
        isRefreshingBackAnim: Boolean,
        initTranslationY: Float,
        pullRefreshMaxDistance: Int,
        continuePullIntoTwoFloorDistance: Int,
        headerHeight: Int,
        isNoSecondFloor: Boolean
    ) {
        oneFloorController?.initOneFloorController(
            screenHeight,
            currentItemIndex,
            frictionValue,
            isInterceptOneFloorTouch,
            isRefreshingBackAnim,
            initTranslationY,
            pullRefreshMaxDistance,
            continuePullIntoTwoFloorDistance,
            headerHeight,
            isNoSecondFloor
        )
    }

    fun setGuideAnim() {
        oneFloorController?.setGuideAnim(true, 400, 1500)
    }

    fun setGuideAnim(
        isDown: Boolean = true,
        transitionDuration: Long = 400,
        stayDuration: Long = 1500
    ) {
        oneFloorController?.setGuideAnim(isDown, transitionDuration, stayDuration)
    }

    fun setCurrentItem(index: Int) {
        oneFloorController?.setCurrentItem(index, true)
    }

    fun setCurrentItem(index: Int, isScroll: Boolean) {
        oneFloorController?.setCurrentItem(index, isScroll, 300)
    }

    fun setCurrentItem(index: Int, duration: Long) {
        oneFloorController?.setCurrentItem(index, true, duration)
    }

    /**
     * 获取当前坐标位置
     */
    fun getCurrentItemIndex(): Int =
        oneFloorController?.getCurrentItemIndex() ?: SecondFloorOverController.ONE_FLOOR_INDEX

    /**
     * 获取当前刷新状态
     */
    fun getCurrentRefreshStatus(): Int =
        oneFloorController?.getCurrentRefreshStatus() ?: SecondFloorOverController.REFRESH_HEADER_NO

    /**
     * 获取当前下拉楼状态
     */
    fun getCurrentPullFloorStatus(): Int =
        oneFloorController?.getCurrentPullFloorStatus() ?: SecondFloorOverController.PULL_ONE_FLOOR

    fun isGuideStatus(): Boolean = oneFloorController?.isGuideStatus() ?: false

    /**
     * 刷新完成
     */
    fun setRefreshComplete() {
        setRefreshComplete(false)
    }

    /**
     * 刷新完成
     */
    fun setRefreshComplete(isGoneHeader: Boolean = false) {
        oneFloorController?.setRefreshComplete(isGoneHeader)
    }

    /**
     * 测量子控件
     */
    private fun measureView(child: View?) {
        var p = child?.layoutParams
        if (p == null) {
            p = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val childWidthSpec = getChildMeasureSpec(0, 0, p.width)
        val lpHeight = p.height
        val childHeightSpec: Int
        childHeightSpec = if (lpHeight > 0) {
            MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY)
        } else {
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        }
        child?.measure(childWidthSpec, childHeightSpec)
    }

    /**
     * 添加滑动控件
     */
    fun addScrollListView(view: View?) {
        oneFloorController?.addScrollListView(view)
    }

    /**
     * 添加内容视图
     */
    fun addContentView(view: View?) {
        view?.apply {
            oneFloorController?.addContentView(this)
        }
    }

    /**
     * 添加Header
     */
    fun addHeaderView(view: View?) {
        view?.apply {
            measureView(this)
            val params = LayoutParams(LayoutParams.MATCH_PARENT, measuredHeight)
            addHeaderView(this, params, measuredHeight)
        }
    }

    /**
     * 添加Header
     */
    fun addHeaderView(view: View?, params: LayoutParams?, headerHeight: Int) {
        this.headerHeight = headerHeight
        val marginLayoutParams: MarginLayoutParams? =
            oneFloorController?.layoutParams as MarginLayoutParams?
        marginLayoutParams?.topMargin = -headerHeight
        oneFloorController?.addHeaderView(view, params, headerHeight)
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(view: View?) {
        view?.apply {
            measureView(this)
            addHeaderHeight(measuredHeight)
        }
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(headerHeight: Int) {
        this.headerHeight = headerHeight
        val marginLayoutParams: MarginLayoutParams? =
            oneFloorController?.layoutParams as MarginLayoutParams?
        marginLayoutParams?.topMargin = -headerHeight
        oneFloorController?.addHeaderHeight(headerHeight)
    }

    /**
     * 添加下拉刷新监听
     */
    fun addOnPullRefreshListener(onPullRefreshListener: SecondFloorOverController.OnPullRefreshListener?) {
        oneFloorController?.addOnPullRefreshListener(onPullRefreshListener)
    }

    /**
     * 添加滑动监听
     */
    fun addOnPullScrollListener(onPullScrollListener: SecondFloorOverController.OnPullScrollListener?) {
        oneFloorController?.addOnPullScrollListener(onPullScrollListener)
    }
}