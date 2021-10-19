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
 * 二楼自定义控件
 */
class SecondFloorOverController(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {

    /**
     * 重叠距离
     */
    private var overlapDistance = 0

    /**
     * 屏幕高度
     */
    private var screenHeight: Int = ScreenTools.getScreenHeight(context)

    /**
     * 控件高度
     */
    private var controllerHeight: Int = screenHeight * 2 - overlapDistance

    /**
     * 初始化位置
     */
    private var initTranslationY = -screenHeight.toFloat() + overlapDistance

    /**
     * 一楼控件
     */
    private var oneFloorFrameLayout: FrameLayout? = null

    /**
     * 二楼控件
     */
    private var twoFloorFrameLayout: FrameLayout? = null

    /**
     * Header控件
     */
    private var headerFrameLayout: FrameLayout? = null

    /**
     * 上次滑动的距离
     */
    private var lastDownY = 0f

    /**
     * 当前位置
     */
    private var currentItemIndex = ONE_FLOOR_INDEX

    /**
     * 摩擦力
     */
    private var frictionValue = 2f

    /**
     * 下拉刷新最大距离
     */
    private var pullRefreshMaxDistance = ScreenTools.dip2px(context, 100f)

    /**
     * 继续下拉进入二楼距离
     */
    private var continuePullIntoTwoFloorDistance = ScreenTools.dip2px(context, 200f)

    /**
     * Header高度
     */
    private var headerHeight = 0

    /**
     * 是否拦截一楼触摸事件
     */
    private var isInterceptOneFloorTouch = false

    /**
     * 刷新监听
     */
    private var onPullRefreshListener: OnPullRefreshListener? = null

    /**
     * 滑动监听
     */
    private var onPullScrollListener: OnPullScrollListener? = null

    /**
     * 刷新状态
     */
    private var refreshHeaderStatus = REFRESH_HEADER_NO

    /**
     * 下拉楼状态
     */
    private var pullFloorStatus = PULL_ONE_FLOOR

    /**
     * 是否有header
     */
//    private var isHaveHeader = false

    /**
     * 一楼如果是列表控件需要处理滑动冲突
     */
    private var view: View? = null

    /**
     * 是否RecyclerView滚动到顶部
     */
    private var isSlidingTop = true

    /**
     * 是否拦截该控件
     */
    private var isIntercept = true

    /**
     * 是否刷新回弹
     */
    private var isRefreshingBackAnim = true

    /**
     * 是否一楼覆盖二楼
     */
    private var isOver = false

    /**
     * 是否header位置处于一楼头顶
     */
    private var isHeaderInOneFloorTop = true

    private var lastDistance = 0f

    /**
     * 是否动画运行中
     */
    private var isAnimRunning = false

    /**
     * 是否引导状态
     */
    private var isGuideStatus = false

    /**
     * 引导动画
     */
    private var guideAnimator: ValueAnimator? = null

    /**
     * 是否向下引导，因为引导动画执行是先向下然后向上
     */
    private var isDownGuide = true

    private val mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop

    private var mScrollPointerId: Int = -1

    private var touchX = 0f

    private var touchY = 0f

    private var lastMoveDistanceY = 0f

    companion object {

        /**
         * 一楼坐标
         */
        const val ONE_FLOOR_INDEX = 0

        /**
         * 二楼坐标
         */
        const val TWO_FLOOR_INDEX = 1

        /**
         * 不刷新
         */
        const val REFRESH_HEADER_NO = 0

        /**
         * 准备刷新
         */
        const val REFRESH_HEADER_PREPARE = 1

        /**
         * 正在刷新
         */
        const val REFRESH_HEADER_RUNNING = 2

        /**
         * 继续下拉进入二楼
         */
        const val REFRESH_HEADER_TWO_FLOOR_PREPARE = 3

        /**
         * 松开进入二楼
         */
        const val REFRESH_HEADER_TWO_FLOOR_RUNNING = 4

        /**
         * 刷新完成
         */
        const val REFRESH_HEADER_END = 5

        /**
         * 下拉到二楼
         */
        const val PULL_SECOND_FLOOR = 6

        /**
         * 拉到一楼
         */
        const val PULL_ONE_FLOOR = 7

        /**
         * 下拉到二楼运行时
         */
        const val PULL_SECOND_FLOOR_RUNNING = 8

        /**
         * 拉到一楼运行时
         */
        const val PULL_ONE_FLOOR_RUNNING = 9
    }

    init {
        val ta: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SecondFloorOverController)
        frictionValue =
            ta.getFloat(R.styleable.SecondFloorOverController_sfc_friction_value, frictionValue)
        overlapDistance = ta.getDimension(
            R.styleable.SecondFloorOverController_sfc_overlap_distance,
            overlapDistance.toFloat()
        ).toInt()
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
        isOver = ta.getBoolean(
            R.styleable.SecondFloorOverController_sfc_is_over,
            isOver
        )
        ta.recycle()

        controllerHeight = if (isOver) {
            screenHeight
        } else {
            screenHeight * 2 - overlapDistance
        }
        initTranslationY = -screenHeight.toFloat() + overlapDistance

        if (isOver) {
            oneFloorFrameLayout = OneFloorController(context, attrs)
            oneFloorFrameLayout?.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, screenHeight)
        } else {
            oneFloorFrameLayout = FrameLayout(context)
            oneFloorFrameLayout?.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, screenHeight)
        }

        twoFloorFrameLayout = FrameLayout(context)
        twoFloorFrameLayout?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, screenHeight)

        headerFrameLayout = FrameLayout(context)
        headerFrameLayout?.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, pullRefreshMaxDistance)
        headerFrameLayout?.visibility = View.GONE

        addView(twoFloorFrameLayout)
        addView(headerFrameLayout)
        addView(oneFloorFrameLayout)

        val oneMarginLayoutParams: MarginLayoutParams? =
            oneFloorFrameLayout?.layoutParams as MarginLayoutParams?
        val twoMarginLayoutParams: MarginLayoutParams? =
            twoFloorFrameLayout?.layoutParams as MarginLayoutParams?
        val headerMarginLayoutParams: MarginLayoutParams? =
            headerFrameLayout?.layoutParams as MarginLayoutParams?
        if (isOver) {
            overlapDistance = 0
            oneMarginLayoutParams?.topMargin = 0
            twoMarginLayoutParams?.topMargin = 0
        } else {
            oneMarginLayoutParams?.topMargin = screenHeight - overlapDistance
            twoMarginLayoutParams?.topMargin = 0
        }
        headerMarginLayoutParams?.topMargin = screenHeight - pullRefreshMaxDistance

        (oneFloorFrameLayout as? OneFloorController)?.initOneFloorController(
            headerFrameLayout,
            screenHeight,
            currentItemIndex,
            frictionValue,
            isInterceptOneFloorTouch,
            isRefreshingBackAnim,
            initTranslationY,
            pullRefreshMaxDistance,
            continuePullIntoTwoFloorDistance,
            headerHeight
        )

        if (isOver) {
            when (currentItemIndex) {
                ONE_FLOOR_INDEX -> {
                }
                TWO_FLOOR_INDEX -> {
                    setCurrentItem(currentItemIndex, false)
                }
            }
        } else {
            when (currentItemIndex) {
                ONE_FLOOR_INDEX -> {
                    pullFloorStatus = PULL_ONE_FLOOR
                    ViewHelper.setTranslationY(this, initTranslationY)
                }
                TWO_FLOOR_INDEX -> {
                    pullFloorStatus = PULL_SECOND_FLOOR
                    setCurrentItem(currentItemIndex, false)
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        closeGuideAnim()
        if (refreshHeaderStatus == REFRESH_HEADER_RUNNING || pullFloorStatus == PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (!isOver && getCurrentItemIndex() == ONE_FLOOR_INDEX && ScrollingUtil.isViewToTop(view,mTouchSlop) && !isAnimRunning) {
            onFloorTouch(event, true)
        } else {
            super.dispatchTouchEvent(event)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        closeGuideAnim()
        if (refreshHeaderStatus == REFRESH_HEADER_RUNNING || pullFloorStatus == PULL_ONE_FLOOR_RUNNING) {
            return false
        }
        return if (!isOver && getCurrentItemIndex() == ONE_FLOOR_INDEX && ScrollingUtil.isViewToTop(view,mTouchSlop) && !isAnimRunning) {
            onFloorTouch(event, false)
        } else {
            super.onTouchEvent(event)
        }
    }

    /**
     * 一楼事件处理
     */
    private fun onFloorTouch(event: MotionEvent?, isDispatchTouch: Boolean): Boolean {
        if (isInterceptOneFloorTouch || refreshHeaderStatus == REFRESH_HEADER_RUNNING) return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
            event
        )
        val actionIndex = event?.actionIndex ?: 0
        if (!isTouchEvent(event) && ViewHelper.getTranslationY(this) == initTranslationY) {
            if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
                mScrollPointerId = event.getPointerId(0)
                touchX = event.rawX
                touchY = event.rawY
                refreshHeaderStatus = REFRESH_HEADER_NO
                lastDownY = event.rawY
            }
            if (event?.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getRawX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getRawY(event.findPointerIndex(mScrollPointerId))
                refreshHeaderStatus = REFRESH_HEADER_NO
                lastDownY = event.getRawY(event.findPointerIndex(mScrollPointerId))
            }
            return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
                event
            )
        }
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mScrollPointerId = event.getPointerId(0)
                touchX = event.rawX
                touchY = event.rawY
                if (refreshHeaderStatus == REFRESH_HEADER_NO || refreshHeaderStatus == REFRESH_HEADER_END) {
                    refreshHeaderStatus = REFRESH_HEADER_NO
                    lastDownY = event.rawY
                } else {
                    return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
                        event
                    )
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getRawX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getRawY(event.findPointerIndex(mScrollPointerId))
                lastDownY = event.getRawY(event.findPointerIndex(mScrollPointerId))
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(mScrollPointerId)
                if (index < 0) return false
                view?.parent?.requestDisallowInterceptTouchEvent(false)
                val moveY = (event.getRawY(index) - lastDownY) / frictionValue
                when (currentItemIndex) {
                    ONE_FLOOR_INDEX -> { // 1楼
                        // 底部临界
                        if (ViewHelper.getTranslationY(this) + moveY <= initTranslationY) {
                            ViewHelper.setTranslationY(this, initTranslationY)
                        } else {
                            // 滑动显示头部布局
                            setHeaderVisible(true)
                            val moveDistanceY =
                                lastMoveDistanceY + (event.getRawY(index) - lastDownY) / frictionValue
                            lastMoveDistanceY = moveDistanceY
                            lastDownY = event.getRawY(index)
                            // 滑动监听
                            onPullScrollListener?.onPullScroll(
                                moveDistanceY,
                                moveY
                            )
                            // 下拉刷新开始
                            if (moveDistanceY <= pullRefreshMaxDistance && refreshHeaderStatus != REFRESH_HEADER_PREPARE) {
                                refreshHeaderStatus = REFRESH_HEADER_PREPARE
                                onPullRefreshListener?.onPullStatus(REFRESH_HEADER_PREPARE)
                            }
                            // 进入二楼准备
                            if (moveDistanceY > pullRefreshMaxDistance && moveDistanceY <= continuePullIntoTwoFloorDistance && refreshHeaderStatus != REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                                refreshHeaderStatus =
                                    REFRESH_HEADER_TWO_FLOOR_PREPARE
                                onPullRefreshListener?.onPullStatus(
                                    REFRESH_HEADER_TWO_FLOOR_PREPARE
                                )
                            }
                            // 松开进入二楼
                            if (moveDistanceY > continuePullIntoTwoFloorDistance && refreshHeaderStatus != REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                                refreshHeaderStatus =
                                    REFRESH_HEADER_TWO_FLOOR_RUNNING
                                onPullRefreshListener?.onPullStatus(
                                    REFRESH_HEADER_TWO_FLOOR_RUNNING
                                )
                            }
                            // 滑动距离设置
                            ViewHelper.setTranslationY(
                                this,
                                ViewHelper.getTranslationY(this) + moveY
                            )
                        }
                    }
                    TWO_FLOOR_INDEX -> { // 2楼

                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                lastMoveDistanceY = 0f
                // 如果松开的时候处于进入二楼准备阶段，则进行刷新操作
                if (refreshHeaderStatus == REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                    setRefreshing()
                } else {
                    // 当处于1楼的时候
                    if (currentItemIndex == ONE_FLOOR_INDEX) {
                        // 如果松开的距离大于进入二楼临界值时，直接进入二楼，否则回到一楼
                        if (refreshHeaderStatus == REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                            setCurrentItem(TWO_FLOOR_INDEX)
                        } else {
                            setCurrentItem(ONE_FLOOR_INDEX)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_CANCEL -> {
                if (refreshHeaderStatus != REFRESH_HEADER_RUNNING) {
                    setCurrentItem(ONE_FLOOR_INDEX, false)
                }
            }
        }
        return if (view == null || !isDispatchTouch) true else super.dispatchTouchEvent(
            event
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        if (isOver || view == null || getCurrentItemIndex() == TWO_FLOOR_INDEX) return super.onInterceptTouchEvent(
            event
        )
        val actionIndex = event?.actionIndex ?: 0
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                mScrollPointerId = event.getPointerId(actionIndex)
                touchX = event.getRawX(event.findPointerIndex(mScrollPointerId))
                touchY = event.getRawY(event.findPointerIndex(mScrollPointerId))
                isIntercept = false
            }
            MotionEvent.ACTION_MOVE -> {
                isIntercept =
                    ViewHelper.getTranslationY(this) + screenHeight - overlapDistance > 0f && ScrollingUtil.isViewToTop(view,mTouchSlop) && isTouchEvent(
                        event
                    )
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
        val x = event?.getRawX(index) ?: 0f
        val y = event?.getRawY(index) ?: 0f
        val dx = x - touchX
        val dy = y - touchY
        return abs(dy) > mTouchSlop && abs(dy) >= abs(dx)
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
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.setGuideAnim(
                isDown,
                transitionDuration,
                stayDuration
            )
        } else {
            this.isDownGuide = isDown
            this.isGuideStatus = true
            lastDistance = ViewHelper.getTranslationY(this)
            val lastValue = continuePullIntoTwoFloorDistance
            guideAnimator = ValueAnimator.ofFloat(
                if (isDown) initTranslationY else initTranslationY + lastValue.toFloat(),
                if (isDown) initTranslationY + lastValue.toFloat() else initTranslationY
            )
            guideAnimator?.addUpdateListener {
                val value: Float = it.animatedValue as Float
                lastMoveDistanceY = value - initTranslationY
                // 下拉刷新开始
                if ((value - initTranslationY) <= pullRefreshMaxDistance && refreshHeaderStatus != REFRESH_HEADER_PREPARE) {
                    refreshHeaderStatus = REFRESH_HEADER_PREPARE
                    onPullRefreshListener?.onPullStatus(REFRESH_HEADER_PREPARE)
                }
                // 进入二楼准备
                if ((value - initTranslationY) > pullRefreshMaxDistance && value <= continuePullIntoTwoFloorDistance && refreshHeaderStatus != REFRESH_HEADER_TWO_FLOOR_PREPARE) {
                    refreshHeaderStatus =
                        REFRESH_HEADER_TWO_FLOOR_PREPARE
                    onPullRefreshListener?.onPullStatus(
                        REFRESH_HEADER_TWO_FLOOR_PREPARE
                    )
                }
                // 松开进入二楼
                if ((value - initTranslationY) > continuePullIntoTwoFloorDistance && refreshHeaderStatus != REFRESH_HEADER_TWO_FLOOR_RUNNING) {
                    refreshHeaderStatus =
                        REFRESH_HEADER_TWO_FLOOR_RUNNING
                    onPullRefreshListener?.onPullStatus(
                        REFRESH_HEADER_TWO_FLOOR_RUNNING
                    )
                }
                ViewHelper.setTranslationY(this, value)
                onPullScrollListener?.onPullScroll(
                    (value - initTranslationY),
                    lastDistance - value
                )
                lastDistance = value
            }
            guideAnimator?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    refreshHeaderStatus = REFRESH_HEADER_NO
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
                    refreshHeaderStatus = REFRESH_HEADER_NO
                    isDownGuide = false
                    isGuideStatus = false
                }
            })
            guideAnimator?.interpolator = DecelerateInterpolator()
            guideAnimator?.duration = transitionDuration
            guideAnimator?.start()
        }
    }

    /**
     * 添加一楼视图
     */
    fun addOneFloorView(view: View?) {
        view?.apply {
            oneFloorFrameLayout?.addView(this)
        }
    }

    /**
     * 添加二楼视图
     */
    fun addTwoFloorView(view: View?) {
        view?.apply {
            twoFloorFrameLayout?.addView(this)
        }
    }

    /**
     * 添加头部
     */
    fun addHeaderView(view: View?) {
        view?.apply {
            measureView(this)
            headerHeight = measuredHeight
            val params = LayoutParams(LayoutParams.MATCH_PARENT, headerHeight)
            params.gravity = Gravity.BOTTOM
            if (isOver) {
                (oneFloorFrameLayout as? OneFloorController)?.addHeaderView(this, params)
            } else {
                headerFrameLayout?.addView(this, params)
            }
        }
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(headerHeight: Int) {
        this.headerHeight = headerHeight
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.addHeaderHeight(headerHeight)
        }
    }

    /**
     * 如果要使用外部header需要将header高度传入
     */
    fun addHeaderHeight(view: View?) {
        view?.apply {
            measureView(this)
            headerHeight = measuredHeight
            if (isOver) {
                (oneFloorFrameLayout as? OneFloorController)?.addHeaderHeight(headerHeight)
            }
        }
    }

    /**
     * 添加滚动的列表View
     */
    fun addScrollListView(view: View?) {
        if (!isOver) {
            this.view = view
        } else {
            (oneFloorFrameLayout as? OneFloorController)?.addScrollListView(view)
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
     * 设置重叠距离
     * @param overlapDistance
     *          重叠距离
     */
    fun setOverlapDistance(overlapDistance: Int) {
        if (!isOver) {
            this.overlapDistance = overlapDistance
            controllerHeight = screenHeight * 2 - overlapDistance
            initTranslationY = -screenHeight.toFloat() + overlapDistance
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, controllerHeight)
            val oneMarginLayoutParams: MarginLayoutParams? =
                oneFloorFrameLayout?.layoutParams as MarginLayoutParams?
            oneMarginLayoutParams?.topMargin = screenHeight - overlapDistance
            ViewHelper.setTranslationY(this, initTranslationY)
            requestLayout()
        }
    }

    /**
     * 设置重叠距离
     * @param view
     *      高度计算的控件
     */
    fun setOverlapDistance(view: View?) {
        if (!isOver) {
            measureView(view)
            this.overlapDistance = view?.measuredHeight ?: 0
            controllerHeight = screenHeight * 2 - overlapDistance
            initTranslationY = -screenHeight.toFloat() + overlapDistance
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, controllerHeight)
            val oneMarginLayoutParams: MarginLayoutParams? =
                oneFloorFrameLayout?.layoutParams as MarginLayoutParams?
            oneMarginLayoutParams?.topMargin = screenHeight - overlapDistance
            ViewHelper.setTranslationY(this, initTranslationY)
            requestLayout()
        }
    }

    /**
     * 获取重叠距离
     */
    fun getOverlapDistance(): Int = overlapDistance

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
     * 设置要显示的页面
     * @param index
     *          设置的位置
     * @param isScroll
     *          是否滚动动画
     * @param duration
     *          动画执行事件
     */
    fun setCurrentItem(index: Int, isScroll: Boolean = true, duration: Long = 300) {
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.setCurrentItem(index, isScroll)
        } else {
            if (index > 1 || index < 0) return
            if (isAnimRunning) return
            this.isAnimRunning = true
            this.currentItemIndex = index
            lastMoveDistanceY = 0f
            when (index) {
                ONE_FLOOR_INDEX -> { // 1楼
                    pullFloorStatus = PULL_ONE_FLOOR_RUNNING
                    onPullRefreshListener?.onPullFloorStatus(PULL_ONE_FLOOR_RUNNING)
                    this.isInterceptOneFloorTouch = false
                    lastDistance =
                        if (isOver) ViewHelper.getTranslationY(oneFloorFrameLayout) else ViewHelper.getTranslationY(
                            this
                        )
                    val animator =
                        ValueAnimator.ofFloat(lastDistance, initTranslationY)
                    animator.addUpdateListener {
                        val value: Float = it.animatedValue as Float
                        ViewHelper.setTranslationY(this, value)
                        onPullScrollListener?.onPullScroll(
                            screenHeight + value - overlapDistance,
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
                            pullFloorStatus = PULL_ONE_FLOOR
                            onPullRefreshListener?.onPullFloorStatus(PULL_ONE_FLOOR)
                            refreshHeaderStatus = REFRESH_HEADER_NO
                            isAnimRunning = false
                        }
                    })
                    animator.start()
                    setHeaderVisible(false)
                }
                TWO_FLOOR_INDEX -> { // 2楼
                    pullFloorStatus = PULL_SECOND_FLOOR_RUNNING
                    onPullRefreshListener?.onPullFloorStatus(PULL_SECOND_FLOOR_RUNNING)
                    this.isInterceptOneFloorTouch = true
                    val animator = ViewPropertyAnimator.animate(this).y(0f)
                    animator.setInterpolator(DecelerateInterpolator())
                    if (isScroll) {
                        animator.duration = duration
                    } else {
                        animator.duration = 0
                    }
                    animator.setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            pullFloorStatus = PULL_SECOND_FLOOR
                            onPullRefreshListener?.onPullFloorStatus(PULL_SECOND_FLOOR)
                            refreshHeaderStatus = REFRESH_HEADER_NO
                            isAnimRunning = false
                        }
                    })
                    animator.start()
                    // 进入二楼设置header隐藏
                    setHeaderVisible(false)
                }
            }
        }
    }

    /**
     * 设置刷新中状态
     */
    private fun setRefreshing() {
        refreshHeaderStatus = REFRESH_HEADER_RUNNING
        onPullRefreshListener?.onPullStatus(REFRESH_HEADER_RUNNING)
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
                    screenHeight + value - overlapDistance,
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
     * 刷新完成
     */
    fun setRefreshComplete() {
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.setRefreshComplete()
        } else {
            refreshHeaderStatus = REFRESH_HEADER_END
            headerFrameLayout?.visibility = View.GONE
            setCurrentItem(ONE_FLOOR_INDEX)
            view?.parent?.requestDisallowInterceptTouchEvent(true)
            onPullRefreshListener?.onPullStatus(REFRESH_HEADER_END)
        }
    }

    /**
     * 获取当前坐标位置
     */
    fun getCurrentItemIndex(): Int =
        if (isOver) (oneFloorFrameLayout as? OneFloorController)?.getCurrentItemIndex()
            ?: ONE_FLOOR_INDEX else currentItemIndex

    /**
     * 获取当前刷新状态
     */
    fun getCurrentRefreshStatus(): Int =
        if (isOver) (oneFloorFrameLayout as? OneFloorController)?.getCurrentRefreshStatus()
            ?: REFRESH_HEADER_NO else refreshHeaderStatus

    /**
     * 获取当前下拉楼状态
     */
    fun getCurrentPullFloorStatus(): Int =
        if (isOver) (oneFloorFrameLayout as? OneFloorController)?.getCurrentPullFloorStatus()
            ?: PULL_ONE_FLOOR else pullFloorStatus

    fun isGuideStatus(): Boolean = isGuideStatus

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, controllerHeight)
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
     * 添加下拉刷新监听
     */
    fun addOnPullRefreshListener(onPullRefreshListener: OnPullRefreshListener?) {
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.addOnPullRefreshListener(
                onPullRefreshListener
            )
        } else {
            this.onPullRefreshListener = onPullRefreshListener
        }
    }

    /**
     * 添加滑动监听
     */
    fun addOnPullScrollListener(onPullScrollListener: OnPullScrollListener?) {
        if (isOver) {
            (oneFloorFrameLayout as? OneFloorController)?.addOnPullScrollListener(
                onPullScrollListener
            )
        } else {
            this.onPullScrollListener = onPullScrollListener
        }
    }

    /**
     * 下拉刷新状态监听
     */
    interface OnPullRefreshListener {
        fun onPullStatus(status: Int)
        fun onPullFloorStatus(status: Int)
    }

    /**
     * 下拉滚动距离监听
     */
    interface OnPullScrollListener {
        fun onPullScroll(scrollY: Float, scrollDistance: Float)
    }

}