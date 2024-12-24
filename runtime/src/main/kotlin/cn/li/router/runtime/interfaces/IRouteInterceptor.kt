package cn.li.router.runtime.interfaces

import cn.li.router.runtime.RouteNavigator


/**
 *
 * @author suweikai 2024/12/24
 */
interface IRouteInterceptor {

    /**
     * 匹配是否要拦截
     * @return true 标识要拦截，即将调用 [onIntercept] 方法
     * */
    fun match(navigator: RouteNavigator): Boolean

    /**
     * 拦截动作
     * @return null 标识中断导航动作，或者返回拦截后新的 [RouteNavigator]
     * */
    fun onIntercept(navigator: RouteNavigator): RouteNavigator?

}

