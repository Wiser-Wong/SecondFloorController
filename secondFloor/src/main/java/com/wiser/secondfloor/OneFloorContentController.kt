package com.wiser.secondfloor

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.wiser.secondfloor.nineoldandroids.animation.Animator
import com.wiser.secondfloor.nineoldandroids.animation.AnimatorListenerAdapter
import com.wiser.secondfloor.nineoldandroids.animation.ValueAnimator
import com.wiser.secondfloor.nineoldandroids.view.ViewHelper
import com.wiser.secondfloor.nineoldandroids.view.ViewPropertyAnimator
import kotlin.math.abs

/**
 * @author Wiser
 *
 * 一楼触摸事件
 */
class OneFloorContentController(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {

    /**
     * 屏幕高度
     */
    private var screenHeight: Int = ScreenTools.getScreenHeight(context)

    /**
     * 一楼如果是列表控件需要处理滑动冲突
     */
    private var view: View? = null

    /**
     * Header控件
     */
    private var headerFrameLayout: FrameLayout? = null

    /**
     * 上次滑动的距离
     */
    private var lastDownY = 0f

    private var lastMoveDistanceY = 0f

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
     * 是否禁止二楼
     */
    private var isNoSecondFloor = false

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

    /**
     * 引导动画
     */
    private var guideAnimator: ValueAnimator? = null

    /**
     * 是否向下引导，因为引导动画执行是先向下然后向上
     */
    private var isDownGuide = true

    /**
     * 是否引导状态
     */
    private var isGuideStatus = false

    private val mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop

    private var mScrollPointerId: Int = -1

    private var touchX = 0f

    private var touchY = 0f

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
        isNoSecondFloor = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_no_second_floor,
            isNoSecondFloor
        )
        ta.recycle()

        initTranslationY = -screenHeight.toFloat()

        headerFrameLayout = FrameLayout(context)
        headerFrameLayout?.layoutParams =
            LayoutParams(
                LayoutParams.MATCH_PARENT, headerHeight
            )
        headerFrameLayout?.visibility = View.GONE

        addView(headerFrameLayout)
    }

    fun initOneFloorController(
        headerFrameLayout: FrameLayout?,
        screenHeight: Int,
        currentItemIndex: Int,
        frictionValue: Float,
        isInterceptOneFloorTouch: Boolean,
        isRefreshingBackAnim: Boolean,
        initTranslationY: Float,
        pullRefreshMaxDistance: Int,
        continuePullIntoTwoFloorDistance: Int,
        headerHeight: Int
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
        this.headerHeight = headerHeight

        when (currentItemIndex) {
            SecondFloorOverController.ONE_FLOOR_INDEX -> {
                pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR
            }
            SecondFloorOverController.TWO_FLOOR_INDEX -> {
                pullFloorStatus = SecondFloorOverController.PULL_SECOND_FLOOR
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        closeGuideAnim()
        if (pullFloorStatus == SecondFloorOverController.PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX && ScrollingUtil.isViewToTop(
                view,
                mTouchSlop
            ) && !isAnimRunning
        ) {
            onFloorTouch(event, true)
        } else {
            super.dispatchTouchEvent(event)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (view != null) return super.onTouchEvent(event)
        closeGuideAnim()
        if (pullFloorStatus == SecondFloorOverController.PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (getCurrentItemIndex() == SecondFloorOverController.ONE_FLOOR_INDEX && !isAnimRunning
        ) {
            onFloorTouch(event, false)
        } else {
            super.onTouchEvent(event)
        }
    }

    /**
     * 一楼事件处理
     */
    private fun onFloorTouch(event: MotionEvent?, isDispatchTouch: Boolean): Boolean {
        if (isInterceptOneFloorTouch || pullFloorStatus == SecondFloorOverController.PULL_ONE_FLOOR_RUNNING) return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
            event
        )
        val actionIndex = event?.actionIndex ?: 0
        if (!isTouchEvent(event) && ViewHelper.getTranslationY(this) == 0f) {
            if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
                mScrollPointerId = event.getPointerId(0)
                touchX = event.x
                touchY = event.y
                refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                lastDownY = event.y
            }
            if (event?.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getY(event.findPointerIndex(mScrollPointerId))
                lastDownY = event.getY(event.findPointerIndex(mScrollPointerId))
            }
            return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
                event
            )
        }
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mScrollPointerId = event.getPointerId(0)
                touchX = event.x
                touchY = event.y
                if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_END) {
                    refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                }
                lastDownY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getY(event.findPointerIndex(mScrollPointerId))
                lastDownY = event.getY(event.findPointerIndex(mScrollPointerId))
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(mScrollPointerId)
                if (index < 0) return false
                view?.parent?.requestDisallowInterceptTouchEvent(false)
                val moveY = (event.getY(index) - lastDownY) / frictionValue
                // 底部临界
                if (ViewHelper.getTranslationY(this) + moveY <= 0) {
                    ViewHelper.setTranslationY(this, 0f)
                } else {
                    // 滑动显示头部布局
                    setHeaderVisible(true)
                    val moveDistanceY =
                        lastMoveDistanceY + (event.getY(index) - lastDownY) / frictionValue
                    lastMoveDistanceY = moveDistanceY
                    lastDownY = event.getY(index)
                    // 是否禁止二楼
                    if (isNoSecondFloor) {
                        if (lastMoveDistanceY >= continuePullIntoTwoFloorDistance) {
                            lastDownY = event.getY(index)
                            lastMoveDistanceY = continuePullIntoTwoFloorDistance.toFloat()
                            return true
                        }
                    }
                    // 滑动监听
                    onPullScrollListener?.onPullScroll(
                        moveDistanceY,
                        moveY
                    )
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
            MotionEvent.ACTION_UP -> {
                // 如果松开的时候处于进入二楼准备阶段，则进行刷新操作
                if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                    setRefreshing()
                } else {
                    // 当处于1楼的时候
                    if (currentItemIndex == SecondFloorOverController.ONE_FLOOR_INDEX) {
                        // 如果松开的距离大于进入二楼临界值时，直接进入二楼，否则回到一楼
                        if (refreshHeaderStatus == SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                            setCurrentItem(SecondFloorOverController.TWO_FLOOR_INDEX)
                        } else {
                            setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_CANCEL -> {
                if (refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_RUNNING) {
                    setCurrentItem(SecondFloorOverController.ONE_FLOOR_INDEX, false)
                }
            }
        }
        return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
            event
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (view == null || getCurrentItemIndex() == SecondFloorOverController.TWO_FLOOR_INDEX) return super.onInterceptTouchEvent(
            event
        )
        val actionIndex = event?.actionIndex ?: 0
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getY(event.findPointerIndex(mScrollPointerId))
                isIntercept = false
            }
            MotionEvent.ACTION_MOVE -> {
                isIntercept =
                    ViewHelper.getTranslationY(this) > 0f && ScrollingUtil.isViewToTop(
                        view,
                        mTouchSlop
                    ) && isTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> isIntercept = false
        }
        return isIntercept
    }

    private fun isTouchEvent(event: MotionEvent?): Boolean {
        val index = event?.findPointerIndex(mScrollPointerId) ?: -1
        if (index < 0) {
            return false
        }
        val x = event?.getX(index) ?: 0f
        val y = event?.getY(index) ?: 0f
        val dx = x - touchX
        val dy = y - touchY
        return abs(dy) > mTouchSlop && abs(dy) >= abs(dx)
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
                    headerHeight.toFloat()
                )
            animator.addUpdateListener {
                val value: Float = it.animatedValue as Float
                ViewHelper.setTranslationY(this, value)
                onPullScrollListener?.onPullScroll(
                    value,
                    lastDistance - value
                )
                lastDistance = value
            }
            animator.interpolator = DecelerateInterpolator()
            animator.duration = 300
            animator.start()
        }
    }

    /**
     * 关闭引导动画
     */
    fun closeGuideAnim() {
        if (isGuideStatus && guideAnimator != null) {
            isGuideStatus = false
            isDownGuide = false
            guideAnimator?.cancel()
        }
    }

    fun setGuideAnim() {
        setGuideAnim(true, 400, 1500)
    }

    /**
     * 设置引导动画
     */
    fun setGuideAnim(
        isDown: Boolean = true,
        transitionDuration: Long = 400,
        stayDuration: Long = 1500
    ) {
        this.isDownGuide = isDown
        this.isGuideStatus = true
        lastDistance = ViewHelper.getTranslationY(this)
        val lastValue = continuePullIntoTwoFloorDistance
        if (guideAnimator == null) {
            guideAnimator =
                ValueAnimator.ofFloat(
                    if (isDown) 0f else lastValue.toFloat(),
                    if (isDown) lastValue.toFloat() else 0f
                )
        }
        guideAnimator?.addUpdateListener {
            val value: Float =
                if (isDown) (it.animatedValue as Float) else (lastValue - it.animatedValue as Float)
            lastMoveDistanceY = value
            // 显示头部布局
            setHeaderVisible(true)
            // 下拉刷新开始
            if (value <= pullRefreshMaxDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_PREPARE) {
                refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_PREPARE
                onPullRefreshListener?.onPullStatus(SecondFloorOverController.REFRESH_HEADER_PREPARE)
            }
            // 进入二楼准备
            if (value > pullRefreshMaxDistance && value <= continuePullIntoTwoFloorDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                refreshHeaderStatus =
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE
                onPullRefreshListener?.onPullStatus(
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE
                )
            }
            // 松开进入二楼
            if (value > continuePullIntoTwoFloorDistance && refreshHeaderStatus != SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                refreshHeaderStatus =
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING
                onPullRefreshListener?.onPullStatus(
                    SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING
                )
            }
            ViewHelper.setTranslationY(this, value)
            onPullScrollListener?.onPullScroll(
                value,
                lastDistance - value
            )
            lastDistance = value
        }
        guideAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                if (isDownGuide) {
                    Handler().postDelayed({
                        if (isDownGuide) {
                            setGuideAnim(false)
                        }
                    }, stayDuration)
                } else {
                    isGuideStatus = false
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
                refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
                isDownGuide = false
                isGuideStatus = false
            }
        })
        guideAnimator?.interpolator = DecelerateInterpolator()
        guideAnimator?.duration = transitionDuration
        guideAnimator?.start()
    }

    fun setCurrentItem(index: Int) {
        setCurrentItem(index, true)
    }

    fun setCurrentItem(index: Int, isScroll: Boolean) {
        setCurrentItem(index, isScroll, 300)
    }

    fun setCurrentItem(index: Int, duration: Long) {
        setCurrentItem(index, true, duration)
    }

    /**
     * @param index
     *          设置的位置
     * @param isScroll
     *          是否滚动动画
     * 设置要显示的页面
     */
    fun setCurrentItem(index: Int, isScroll: Boolean = true, duration: Long = 300) {
        if (index > 1 || index < 0) return
        if (isAnimRunning) return
        this.isAnimRunning = true
        this.currentItemIndex = index
        lastMoveDistanceY = 0f
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
                        value,
                        lastDistance - value
                    )
                    lastDistance = value
                }
                animator.interpolator = DecelerateInterpolator()
                if (isScroll) {
                    animator.duration = duration
                } else {
                    animator.duration = 0
                }
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        pullFloorStatus = SecondFloorOverController.PULL_ONE_FLOOR
                        onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_ONE_FLOOR)
                        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
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
                    animator.duration = duration
                } else {
                    animator.duration = 0
                }
                animator.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        pullFloorStatus = SecondFloorOverController.PULL_SECOND_FLOOR
                        onPullRefreshListener?.onPullFloorStatus(SecondFloorOverController.PULL_SECOND_FLOOR)
                        refreshHeaderStatus = SecondFloorOverController.REFRESH_HEADER_NO
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
        view?.parent?.requestDisallowInterceptTouchEvent(true)
        onPullRefreshListener?.onPullStatus(SecondFloorOverController.REFRESH_HEADER_END)
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

    fun isGuideStatus(): Boolean = isGuideStatus

    /**
     * 添加滑动控件
     */
    fun addScrollListView(view: View?) {
        this.view = view
    }

    /**
     * 添加Header
     */
    fun addHeaderView(view: View?) {
        view?.apply {
            measureView(this)
            headerHeight = measuredHeight
            val params = LayoutParams(LayoutParams.MATCH_PARENT, headerHeight)
            val headerMarginLayoutParams: MarginLayoutParams? =
                headerFrameLayout?.layoutParams as MarginLayoutParams?
            headerMarginLayoutParams?.height = headerHeight
            headerMarginLayoutParams?.topMargin = 0
            headerFrameLayout?.addView(this, params)
        }
    }

    /**
     * 添加Header
     */
    fun addHeaderView(view: View?, params: LayoutParams?, headerHeight: Int) {
        this.headerHeight = headerHeight
        view?.apply {
            val headerMarginLayoutParams: MarginLayoutParams? =
                headerFrameLayout?.layoutParams as MarginLayoutParams?
            headerMarginLayoutParams?.height = headerHeight
            headerMarginLayoutParams?.topMargin = 0
            headerFrameLayout?.addView(this, params)
        }
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(headerHeight: Int) {
        this.headerHeight = headerHeight
        val headerMarginLayoutParams: MarginLayoutParams? =
            headerFrameLayout?.layoutParams as MarginLayoutParams?
        headerMarginLayoutParams?.height = headerHeight
        headerMarginLayoutParams?.topMargin = 0
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(view: View?) {
        view?.apply {
            measureView(this)
            headerHeight = measuredHeight
            val headerMarginLayoutParams: MarginLayoutParams? =
                headerFrameLayout?.layoutParams as MarginLayoutParams?
            headerMarginLayoutParams?.height = headerHeight
            headerMarginLayoutParams?.topMargin = 0
        }
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