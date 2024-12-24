package cn.li.router.runtime

import cn.li.router.LiRouter.logger
import cn.li.router.api.generate.IAutowired
import cn.li.router.api.generate.IRouterModule
import cn.li.router.runtime.interfaces.IRouteInterceptor
import cn.li.router.runtime.meta.RouteMeta
import java.util.ServiceLoader

/**
 *
 * @author suweikai 2024/12/23
 */
object RouterController {
    private const val TAG = "RouterController"

    private val routerMap = HashMap<String, RouteMeta>()
    private val autowiredMap = HashMap<Class<*>, IAutowired>()
    private val interceptors = mutableListOf<IRouteInterceptor>()
    private val interceptorPriority = mutableMapOf<IRouteInterceptor, Int>()

    fun addRouteMeta(item: RouteMeta) {
        synchronized(routerMap) {
            if (routerMap.containsKey(item.url)) {
                throw RuntimeException("router(`${item.url}`) already exists, new: ${item}, existed: ${routerMap[item.url]} ")
            } else {
                routerMap[item.url] = item
            }
        }
    }

    fun getRouteMeta(url: String): RouteMeta? {
        synchronized(routerMap) {
            return routerMap[RouteUrl(url).baseUrl]
        }
    }

    fun getRouteMeta(url: RouteUrl): RouteMeta? {
        synchronized(routerMap) {
            return routerMap[url.baseUrl]
        }
    }

    fun registerAutowired(clazz: Class<*>, autowired: IAutowired) {
        synchronized(autowiredMap) {
            if (autowiredMap.containsKey(clazz)) {
                throw RuntimeException("autowired(${clazz}) already exists, new: ${autowired}, existed: ${autowiredMap[clazz]} ")
            } else {
                autowiredMap[clazz] = autowired
            }
        }
    }

    internal fun autowired(target: Any?) {
        target ?: return
        synchronized(autowiredMap) {
            autowiredMap[target.javaClass]?.let { iAutowired ->
                logger.debug(TAG, "[autowired] ${target.javaClass}")
                iAutowired.autowired(target)
            }
        }
    }

    fun registerInterceptor(interceptor: IRouteInterceptor, priority: Int) {
        synchronized(interceptors) {
            // 保证同一种类型拦截器只注册一次
            if (interceptors.any { it.javaClass == interceptor.javaClass }) {
                return
            }
            // TODO: 可以使用二分优化下
            interceptors.add(interceptor)
            interceptorPriority[interceptor] = priority
            interceptors.sortBy { interceptorPriority[it]!!  }
        }
    }

    fun processIntercept(navigator: RouteNavigator): RouteNavigator? {
        var result: RouteNavigator = navigator
        interceptors.forEach { interceptor ->
            val interceptResult = if (interceptor.match(result)) {
                interceptor.onIntercept(navigator)
            } else {
                result
            }
            if (interceptResult == null) {
                return null
            } else {
                result = interceptResult
            }
        }
        return result
    }

    internal fun loadModuleBySpi() {
        val loader = ServiceLoader.load(IRouterModule::class.java)
        loader.forEach { module ->
            module.initRouter()
            module.initAutowired()
            module.initRouteInterceptor()
        }
    }
}