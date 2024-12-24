package cn.li.router.compiler.utils

import cn.li.router.api.annotations.Router
import cn.li.router.api.annotations.Autowired
import cn.li.router.api.annotations.RouteInterceptor
import cn.li.router.compiler.data.AutowiredMeta
import cn.li.router.compiler.data.RouteInterceptorMeta
import cn.li.router.compiler.data.RouteMeta
import cn.li.router.compiler.ksp.AutowiredVisitor
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 *
 * @author suweikai 2024/12/20
 */

lateinit var logger: KSPLogger

private val autowiredVisitor by lazy {
    AutowiredVisitor()
}
/**
 * 解析 [Router] 注解的信息
 * */
fun KSAnnotated.extractRouteMeta(): RouteMeta {
    val item = RouteMeta()
    parseAnnotationInfo<Router> { name, value ->
        when (name) {
            RouteMeta.URL -> item.url = value?.toString()
            RouteMeta.DESC -> item.desc = value?.toString()
        }
    }
    item.clasName = (this as KSClassDeclaration).qualifiedName?.asString()
    logger.info("[parseRouterAnnotation] parse result: $item")
    return item
}

/**
 * 解析 [Autowired] 注解的信息
 * */
fun KSAnnotated.extractAutowiredMeta(): AutowiredMeta {
    return AutowiredMeta().apply {
        accept(autowiredVisitor, this)
        logger.info("[extractAutowiredMeta] parse result: $this")
    }
}

fun KSAnnotated.extractRouteInterceptorMeta(): RouteInterceptorMeta {
    val item = RouteInterceptorMeta()
    parseAnnotationInfo<RouteInterceptor> { name, value ->
        when (name) {
            RouteInterceptorMeta.PRIORITY -> item.priority = value as? Int ?: 5
        }
    }
    item.ownerQualifiedName = (this as KSClassDeclaration).let {
        it.qualifiedName?.asString()?: it.simpleName.asString()
    }
    logger.info("[extractRouteInterceptorMeta] parse result: $item")
    return item
}

