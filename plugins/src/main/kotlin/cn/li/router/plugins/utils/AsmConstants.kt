package cn.li.router.plugins.utils

/**
 *
 * @author suweikai 2024/12/18
 */
object AsmConstants {

    const val PACKAGE_NAME = "cn.li.router"
    const val PREFIX_ROUTER_MAP = "LiRouterMap__"


    const val ROUTER_INJECT_CLASS_NAME = "cn/li/router/RouterInjectKt.class"
    const val ROUTER_MODULE_CLASS_NAME_PREFIX = "cn/li/router/generate/LiRouterModuleGen__"

    const val SPI_SERVICE_CLASS_NAME = "META-INF/services/cn.li.router.api.generate.ILiRouterModule"
}