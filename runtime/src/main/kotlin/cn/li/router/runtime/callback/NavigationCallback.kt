package cn.li.router.runtime.callback

import cn.li.router.runtime.RouteNavigator

/**
 *
 * @author suweikai 2024/12/21
 */
interface NavigationCallback {

    /**
     * 无法找到路由时回调
     * */
    fun onRouteLost(navigator: RouteNavigator)


    fun onRouteError(navigator: RouteNavigator, throwable: Throwable)

    /**
     * 找到路由时回调
     * */
    fun onRouteFound(navigator: RouteNavigator)


    /**
     * 路由开始跳转
     * */
    fun onRouteNavigate(navigator: RouteNavigator)

}