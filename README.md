# SecondFloorController
二楼样式

## 截图
![images](https://github.com/Wiser-Wong/SecondFloorController/blob/master/images/second_floor.gif)
![images](https://github.com/Wiser-Wong/SecondFloorController/blob/master/images/second_floor_rv.gif)
![images](https://github.com/Wiser-Wong/SecondFloorController/blob/master/images/second_floor_sv.gif)
![images](https://github.com/Wiser-Wong/SecondFloorController/blob/master/images/second_floor_wv.gif)

# 使用方法
   
      <com.wiser.secondfloor.SecondFloorOverController xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:sfc="http://schemas.android.com/apk/res-auto"
          xmlns:tools="http://schemas.android.com/tools"
          android:id="@+id/controller"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          sfc:sfc_friction_value="1.5"
          sfc:sfc_is_intercept_one_floor_touch="false"
          sfc:sfc_is_over="false"
          sfc:sfc_is_refreshing_back_anim="true"
          sfc:sfc_overlap_distance="0dp"
          sfc:sfc_pull_into_two_floor_distance="160dp"
          sfc:sfc_pull_refresh_distance="100dp"
          sfc:sfc_show_item="sfc_one_floor"
          tools:context=".MainActivity">

      </com.wiser.secondfloor.SecondFloorOverController>
      
  ## 配置
      // 控件
      overController = view?.findViewById(R.id.controller)
      // 一楼View
      val oneView = LayoutInflater.from(activity)
               .inflate(R.layout.main_one_floor_layout, overController, false)
      // 二楼View
      val twoView = LayoutInflater.from(activity)
               .inflate(R.layout.main_two_floor_layout, overController, false)
      // Fragment替换
      when (arguments?.getString(SKIP_TYPE)) {
               // recyclerView
               SkipType.RECYCLERVIEW.type -> {
                   childFragmentManager.beginTransaction()
                       .replace(
                           R.id.fl_controller_one_floor,
                           OneFloorHasSecondFloorRecyclerViewFragment.newInstance(),
                           OneFloorHasSecondFloorRecyclerViewFragment::javaClass.name
                       ).commitAllowingStateLoss()
               }
               // ScrollView
               SkipType.SCROLLVIEW.type -> {
                   childFragmentManager.beginTransaction()
                       .replace(
                           R.id.fl_controller_one_floor,
                           OneFloorHasSecondFloorScrollViewFragment.newInstance(),
                           OneFloorHasSecondFloorScrollViewFragment::javaClass.name
                       ).commitAllowingStateLoss()
               }
               // WebView
               SkipType.WEBVIEW.type -> {
                   childFragmentManager.beginTransaction()
                       .replace(
                           R.id.fl_controller_one_floor,
                           OneFloorHasSecondFloorWebViewFragment.newInstance(),
                           OneFloorHasSecondFloorWebViewFragment::javaClass.name
                       ).commitAllowingStateLoss()
               }
               // 没有滑动控件
               SkipType.NOLIST.type -> {
                   childFragmentManager.beginTransaction()
                       .replace(
                           R.id.fl_controller_one_floor,
                           OneFloorHasSecondFloorSimpleFragment.newInstance(),
                           OneFloorHasSecondFloorSimpleFragment::javaClass.name
                       ).commitAllowingStateLoss()
               }
               else -> {
               }
           }
           childFragmentManager.beginTransaction()
               .replace(
                   R.id.fl_controller_two_floor,
                   TwoFloorFragment.newInstance(),
                   TwoFloorFragment::javaClass.name
               ).commitAllowingStateLoss()
           // 添加一楼View
           overController?.addOneFloorView(oneView)
           // 添加二楼View
           overController?.addTwoFloorView(twoView)

           // 头部
           val headerView =
               LayoutInflater.from(activity).inflate(R.layout.pull_header, overController, false)
           // 提示
           val tipView = headerView?.findViewById<TextView>(R.id.tv_pull_tip)
           // 添加头部View
           overController?.addHeaderView(headerView)
           // 添加下拉刷新监听
           overController?.addOnPullRefreshListener(object :
               SecondFloorOverController.OnPullRefreshListener {
               override fun onPullStatus(status: Int) {
                   when (status) {
                       SecondFloorOverController.REFRESH_HEADER_PREPARE -> {
                           overController?.setHeaderVisible(true)
                           tipView?.text = "下拉刷新"
                       }
                       SecondFloorOverController.REFRESH_HEADER_RUNNING -> {
                           tipView?.text = "刷新中"
                           overController?.postDelayed(Runnable {
                               overController?.setRefreshComplete()
                           }, 1500)
                       }
                       SecondFloorOverController.REFRESH_HEADER_END -> {
                           overController?.setHeaderVisible(false)
                           activity?.apply {
                               Toast.makeText(this, "刷新数据了", Toast.LENGTH_SHORT).show()
                           }
                       }
                       SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_PREPARE -> {
                           tipView?.text = "继续下拉有惊喜哦"
                       }
                       SecondFloorOverController.REFRESH_HEADER_TWO_FLOOR_RUNNING -> {
                           tipView?.text = "松手得惊喜"
                       }
                   }
               }

               override fun onPullFloorStatus(status: Int) {

               }
           })
           // 添加滚动监听
           overController?.addOnPullScrollListener(object :
               SecondFloorOverController.OnPullScrollListener {

               override fun onPullScroll(scrollY: Float, scrollDistance: Float) {
                   println("滑动监听--->>$scrollY")
               }
           })                          
# 操作指南
* sfc_friction_value:摩擦力
* sfc_overlap_distance:重叠距离
* sfc_header_height:Header高度
* sfc_show_item:当前显示位置
   * sfc_one_floor
   * sfc_two_floor
* sfc_pull_refresh_distance:下拉刷新最大距离
* sfc_pull_into_two_floor_distance:继续下拉进入二楼距离
* sfc_is_intercept_one_floor_touch:是否拦截一楼触摸事件
* sfc_is_refreshing_back_anim:是否刷新回弹
* sfc_is_over:是否一楼覆盖二楼
* sfc_is_no_second_floor:是否禁止二楼
