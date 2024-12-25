package cn.li.router.compiler.data


/**
 *
 * @author suweikai 2024/12/23
 */
data class RouterModule(
    val routers: List<RouteMeta>,
    val autowiredMetas: List<AutowiredMeta>,
    val interceptorMetas: List<RouteInterceptorMeta>,
    val serviceMetas: List<ServiceMeta>
)