package cn.li.lirouter.interceptors

import cn.li.router.LiRouter
import cn.li.router.api.annotations.RouteInterceptor
import cn.li.router.runtime.RouteNavigator
import cn.li.router.runtime.interfaces.IRouteInterceptor

/**
 *
 * @author suweikai 2024/12/24
 */
@RouteInterceptor(priority = 1)
class LoginInterceptor: IRouteInterceptor {
    override fun match(navigator: RouteNavigator): Boolean {
        return navigator.url.baseUrl == "lirouter://first"
    }

    override fun onIntercept(navigator: RouteNavigator): RouteNavigator {
        return LiRouter.route(navigator.context, "lirouter://login").build()
    }
}