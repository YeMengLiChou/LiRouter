package cn.li.router

import android.content.Context
import cn.li.router.runtime.interfaces.IJsonMapper
import cn.li.router.runtime.logger.IRouterLogger
import cn.li.router.runtime.RouteNavigator
import cn.li.router.runtime.RouterController
import cn.li.router.runtime.logger.DefaultRouterLogger

/**
 *
 * @author suweikai 2024/12/17
 */
object LiRouter {
    const val TAG = "LiRouter"

    internal var logger: IRouterLogger = DefaultRouterLogger
        private set

    var jsonMapper: IJsonMapper? = null
        private set

    @Volatile
    var isInitialized = false
        private set


    @Synchronized
    fun init() {
        require(!isInitialized) {
            "LiRouter is already initialized"
        }
        isInitialized = true
        initLiRouter()
        initAutowire()
        initRouteInterceptor()
        initServiceProvider()
        // TODO: 增量存在问题
//        RouterController.loadModuleBySpi()
    }

    fun setLogger(logger: IRouterLogger): LiRouter {
        this.logger = logger
        return this
    }

    fun setJsonMapper(jsonMapper: IJsonMapper): LiRouter {
        this.jsonMapper = jsonMapper
        return this
    }

    fun route(context: Context, url: String): RouteNavigator.Builder {
        checkInitialized()
        return RouteNavigator.Builder(context, url)
    }

    fun autowire(target: Any) {
        checkInitialized()
        RouterController.autowired(target)
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw RuntimeException("LiRouter is not initialized, please call LiRouter.init() first")
        }
    }
}