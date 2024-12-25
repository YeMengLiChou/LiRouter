package cn.li.router.api.generate

/**
 * 每个模块的信息收集
 * @author suweikai 2024/12/20
 */
interface IRouterModule {

    fun initRouter()

    fun initAutowired()

    fun initRouteInterceptor()

    fun initServiceProvider()
}