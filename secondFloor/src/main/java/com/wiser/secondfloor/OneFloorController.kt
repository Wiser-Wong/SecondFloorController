package com.wiser.secondfloor

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.wiser.secondfloor.nineoldandroids.animation.Animator
import com.wiser.secondfloor.nineoldandroids.animation.AnimatorListenerAdapter
import com.wiser.secondfloor.nineoldandroids.animation.ValueAnimator
import com.wiser.secondfloor.nineoldandroids.view.ViewHelper
import com.wiser.secondfloor.nineoldandroids.view.ViewPropertyAnimator

/**
 * @author Wiser
 *
 * 一楼触摸事件
 */
class OneFloorController(context: Context) : FrameLayout(context, null) {

    /**
     * 屏幕高度
     */
    private var screenHeight: Int = ScreenTools.getScreenHeight(context)

    /**
     * 一楼如果是列表控件需要处理滑动冲突
     */
    private var recyclerView: RecyclerView? = null

    /**
     * Header控件
     */
    private var headerFrameLayout: FrameLayout? = null

    /**
     * 上次滑动的距离
     */
    private var lastDownY = 0f

    /**
     * 按下的距离
     */
    private var pressDownY = 0f

    /**
     * 当前位置
     */
    private var currentItemIndex: Int = SecondFloorOverController.ONE_FLOOR_INDEX

    /**
     * 摩擦力
     */
    private var frictionValue = 2f

    /**
     * 刷新状态
     */
    private var refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO

    /**
     * 下拉楼状态
     */
    private var pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR

    /**
     * 是否RecyclerView滚动到顶部
     */
    private var isSlidingTop = true

    /**
     * 是否拦截该控件
     */
    private var isIntercept = true

    private var isAnimRunning = false

    /**
     * 是否拦截一楼触摸事件
     */
    private var isInterceptOneFloorTouch = false

    /**
     * 是否刷新回弹
     */
    private var isRefreshingBackAnim = true

    /**
     * 初始化位置
     */
    private var initTranslationY = 0f

    private var lastDistance = 0f

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

    /**
     * 刷新监听
     */
    private var onPullRefreshListener: SecondFloorOverController.OnPullRefreshListener? = null

    /**
     * 滑动监听
     */
    private var onPullScrollListener: SecondFloorOverController.OnPullScrollListener? = null

    fun initOneFloorController(
        headerFrameLayout: FrameLayout?,
        screenHeight: Int,
        currentItemIndex: Int,
        frictionValue: Float,
        isInterceptOneFloorTouch: Boolean,
        isRefreshingBackAnim: Boolean,
        initTranslationY: Float,
        pullRefreshMaxDistance: Int,
        continuePullIntoTwoFloorDistance: Int
    ) {
        this.headerFrameLayout = headerFrameLayout
        this.screenHeight = screenHeight
        this.currentItemIndex = currentItemIndex
        this.frictionValue = frictionValue
        this.isInterceptOneFloorTouch = isInterceptOneFloorTouch
        this.isRefreshingBackAnim = isRefreshingBackAnim
        this.initTranslationY = initTranslationY
        this.pullRefreshMaxDistance = pullRefreshMaxDistance
        this.continuePullIntoTwoFloorDistance = continuePullIntoTwoFloorDistance

        when(currentItemIndex) {
            SecondFloorOverController.ONE_FLOOR_INDEX -> {
                pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR
            }
            SecondFloorOverController.TWO_FLOOR_INDEX -> {
                pullFloorStatus = SecondFloorOverController.PULL_SECOND_FLOOR
            }
        }
    }

    /**
     * 添加滑动控件
     */
    fun addRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        this.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) { //当前状态为停止滑动
                    // 判断是否滚动到顶部
                    isSlidingTop = !recyclerView.canScrollVertically(-1)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSlidingTop = !recyclerView.canScrollVertically(-1)
            }
        })
    }

    /**
     * 添加Header
     */
    fun addHeaderView(view: View?, params: LayoutParams?) {
        view?.apply {
            headerFrameLayout?.addView(this, params)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_RUNNING || pullFloorStatus == SecondFloorOverController.PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX && isSlidingTop && !isAnimRunning) {
            onFloorTouch(ev, true)
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_RUNNING || pullFloorStatus == SecondFloorOverController.PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX && isSlidingTop && !isAnimRunning) {
            onFloorTouch(event, false)
        } else {
            super.onTouchEvent(event)
        }
    }

    /**
     * 一楼事件处理
     */
    private fun onFloorTouch(event: MotionEvent?, isDispatchTouch: Boolean): Boolean {
        if (isInterceptOneFloorTouch || refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_RUNNING) return if (recyclerView == null || !isDispatchTouch) false else super.dispatchTouchEvent(
            event
        )
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_NO || refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_END) {
                    refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                    lastDownY = event.rawY
                    pressDownY = event.rawY
                } else {
                    return if (recyclerView == null || !isDispatchTouch) true else super.dispatchTouchEvent(
                        event
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> {
                recyclerView?.parent?.requestDisallowInterceptTouchEvent(false)
                val moveY = (event.rawY - lastDownY) / frictionValue
                lastDownY = event.rawY
                // 底部临界
                if (ViewHelper.getTranslationY(this) + moveY <= 0) {
                    ViewHelper.setTranslationY(this, 0f)
                } else {
                    // 滑动监听
                    onPullScrollListener?.onPullScroll(
                        (event.rawY - pressDownY) / frictionValue,
                        moveY
                    )
                    // 滑动显示头部布局
                    setHeaderVisible(true)
                    val moveDistanceY = event.rawY - pressDownY

                    // 下拉刷新开始
                    if (moveDistanceY <= pullRefreshMaxDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_PREPARE) {
                        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_PREPARE
                        onPullRefreshListener?.onPullStatus(SecondFloorOverController.REFRESH_HEADER_PREPARE)
                    }
                    // 进入二楼准备
                    if (moveDistanceY > pullRefreshMaxDistance && moveDistanceY <= continuePullIntoTwoFloorDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                        refreshHeaderStatus =
                            SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE
                        onPullRefreshListener?.onPullStatus(
                            SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE
                        )
                    }
                    // 松开进入二楼
                    if (moveDistanceY > continuePullIntoTwoFloorDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                        refreshHeaderStatus =
                            SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING
                        onPullRefreshListener?.onPullStatus(
                            SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING
                        )
                    }
                    // 滑动距离设置
                    ViewHelper.setTranslationY(
                        this,
                        ViewHelper.getTranslationY(this) + moveY
                    )
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP -> {
                // 如果松开的时候处于进入二楼准备阶段，则进行刷新操作
                if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                    setRefreshing()
                } else {
                    // 当处于1楼的时候
                    if (currentItemIndex == SecondFloorOverController.ONE_FLOOR_INDEX) {
                        // 如果松开的距离大于进入二楼临界值时，直接进入二楼，否则回到一楼
                        if (event.rawY - pressDownY > continuePullIntoTwoFloorDistance) {
                            setCurrentItem(SecondFloorOverController.TWO_FLOOR_INDEX)
                        } else {
                            setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX)
                        }
                    }
                }
            }
        }
        return if (recyclerView == null || !isDispatchTouch) true else super.dispatchTouchEvent(
            event
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (recyclerView == null || getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) return super.onInterceptTouchEvent(
            event
        )
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isIntercept = false
            MotionEvent.ACTION_MOVE -> isIntercept =
                ViewHelper.getTranslationY(this) > 0f && isSlidingTop
            MotionEvent.ACTION_UP -> isIntercept = false
        }
        return isIntercept
    }

    /**
     * 设置刷新中状态
     */
    private fun setRefreshing() {
        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_RUNNING
        onPullRefreshListener?.onPullStatus(SecondFloorOverController.REFRESH_HEADER_RUNNING)
        if (isRefreshingBackAnim) {
            lastDistance = ViewHelper.getTranslationY(this)
            val animator =
                ValueAnimator.ofFloat(
                    ViewHelper.getTranslationY(this),
                    initTranslationY + headerHeight
                )
            animator.addUpdateListener {
                val value: Float = it.animatedValue as Float
                ViewHelper.setTranslationY(this, value)
                onPullScrollListener?.onPullScroll(
                    screenHeight + value,
                    lastDistance - value
                )
                lastDistance = value
            }
            animator.interpolator = DecelerateInterpolator()
            animator.duration = 300
            animator.start()
        }
    }

    fun setCurrentItem(index: Int) {
        setCurrentItem(index, true)
    }

    /**
     * @param index
     *          设置的位置
     * @param isScroll
     *          是否滚动动画
     * 设置要显示的页面
     */
    fun setCurrentItem(index: Int, isScroll: Boolean = true) {
        if (index > 1 || index < 0) return
        if (isAnimRunning) return
        this.isAnimRunning = true
        this.currentItemIndex = index
        when (index) {
            SecondFloorOverController.ONE_FLOOR_INDEX -> { // 1楼
                pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR_RUNNING
                onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_ONE_FLOOR_RUNNING)
                this.isInterceptOneFloorTouch = false
                lastDistance = ViewHelper.getTranslationY(this)
                val animator =
                    ValueAnimator.ofFloat(lastDistance, 0f)
                animator.addUpdateListener {
                    val value: Float = it.animatedValue as Float
                    ViewHelper.setTranslationY(this, value)
                    onPullScrollListener?.onPullScroll(
                        screenHeight + value,
                        lastDistance - value
                    )
                    lastDistance = value
                }
                animator.interpolator = DecelerateInterpolator()
                if (isScroll) {
                    animator.duration = 300
                } else {
                    animator.duration = 0
                }
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                        pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR
                        onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_ONE_FLOOR)
                        isAnimRunning = false
                    }
                })
                animator.start()
                setHeaderVisible(false)
            }
            SecondFloorOverController.TWO_FLOOR_INDEX -> { // 2楼
                pullFloorStatus = SecondFloorOverController.PULL_SECOND_FLOOR_RUNNING
                onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_SECOND_FLOOR_RUNNING)
                this.isInterceptOneFloorTouch = true
                val animator = ViewPropertyAnimator.animate(this).y(-initTranslationY)
                animator.setInterpolator(DecelerateInterpolator())
                if (isScroll) {
                    animator.duration = 300
                } else {
                    animator.duration = 0
                }
                animator.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                        pullFloorStatus = SecondFloorOverController.PULL_SECOND_FLOOR
                        onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_SECOND_FLOOR)
                        isAnimRunning = false
                    }
                })
                animator.start()
                // 进入二楼设置header隐藏
                setHeaderVisible(false)
            }
        }
    }

    /**
     * 设置头部显隐
     */
    fun setHeaderVisible(isVisible: Boolean) {
        if (isVisible) {
            if (headerFrameLayout?.visibility == View.GONE) {
                headerFrameLayout?.visibility = View.VISIBLE
            }
        } else {
            if (headerFrameLayout?.visibility == View.VISIBLE) {
                headerFrameLayout?.visibility = View.GONE
            }
        }
    }

    /**
     * 刷新完成
     */
    fun setRefreshComplete() {
        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_END
        headerFrameLayout?.visibility = View.GONE
        setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX)
        recyclerView?.parent?.requestDisallowInterceptTouchEvent(true)
        onPullRefreshListener?.onPullStatus(SecondFloorOverController.REFRESH_HEADER_END)
    }

    /**
     * 获取当前坐标位置
     */
    fun getCurrentItemIndex(): Int = currentItemIndex

    /**
     * 获取当前刷新状态
     */
    fun getCurrentRefreshStatus(): Int = refreshHeaderStatus

    /**
     * 获取当前下拉楼状态
     */
    fun getCurrentPullFloorStatus(): Int = pullFloorStatus

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(headerHeight: Int) {
        this.headerHeight = headerHeight
    }

    /**
     * 添加下拉刷新监听
     */
    fun addOnPullRefreshListener(onPullRefreshListener: SecondFloorOverController.OnPullRefreshListener?) {
        this.onPullRefreshListener = onPullRefreshListener
    }

    /**
     * 添加滑动监听
     */
    fun addOnPullScrollListener(onPullScrollListener: SecondFloorOverController.OnPullScrollListener?) {
        this.onPullScrollListener = onPullScrollListener
    }
}