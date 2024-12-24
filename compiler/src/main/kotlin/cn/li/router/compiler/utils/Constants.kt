package cn.li.router.compiler.utils

/**
 *
 * @author suweikai 2024/12/17
 */
object Constants {

    const val ACTIVITY_QUALIFIED_NAME = "android.app.Activity"
    const val FRAGMENT_QUALIFIED_NAME = "android.app.Fragment"
    const val FRAGMENT_X_QUALIFIED_NAME = "androidx.fragment.app.Fragment"

    const val ROUTE_INTERCEPTOR_QUALIFIED_NAME = "cn.li.router.runtime.interfaces.IRouteInterceptor"

    const val GENERATE_PACKAGE_NAME = "cn.li.router.generate"

    const val PREFIX_ROUTER_MAP = "LiRouterModuleGen__"

    const val SUFFIX_ROUTER_AUTOWIRED_IMPL = "__LiRouter__AutowiredImpl"
}