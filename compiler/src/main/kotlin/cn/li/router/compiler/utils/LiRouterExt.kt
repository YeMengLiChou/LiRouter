package cn.li.router.compiler.utils

import cn.li.router.api.annotations.Router
import cn.li.router.api.annotations.Autowired
import cn.li.router.api.annotations.RouteInterceptor
import cn.li.router.api.annotations.Service
import cn.li.router.api.interfaces.IServiceProvider
import cn.li.router.compiler.data.AutowiredMeta
import cn.li.router.compiler.data.RouteInterceptorMeta
import cn.li.router.compiler.data.RouteMeta
import cn.li.router.compiler.data.ServiceMeta
import cn.li.router.compiler.ksp.AutowiredVisitor
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlin.reflect.KClass

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


fun KSAnnotated.extractServiceMeta(): ServiceMeta {
    val item = ServiceMeta()
    parseAnnotationInfo<Service> { name, value ->
        when (name) {
            ServiceMeta.SINGLETON -> item.isSingleton = value as? Boolean?: false
            ServiceMeta.RETURN_TYPE -> {
                val returnType = (value as KSType).declaration
                item.returnType = (returnType.qualifiedName ?: returnType.simpleName).asString()
            }
        }
    }
    item.ksFile = this.containingFile!!
    item.isFunction = this is KSFunctionDeclaration
    item.isObject = this is KSClassDeclaration && this.classKind == ClassKind.OBJECT
    if (item.isFunction) {
        this as KSFunctionDeclaration
        // serviceProvider的类型：注解有值则用注解的，否则用方法的返回值
        if (item.returnType.isNotBlank()) {
            item.serviceProviderName = item.returnType
        } else {
            val returnType = this.returnType?.resolve()
            item.serviceProviderName = returnType?.declaration?.let {
                (it.qualifiedName ?: it.simpleName).asString()
            } ?: ""
        }
        item.serviceImplName = (this.qualifiedName ?: this.simpleName).asString()
    } else {
        // 类需要找到其实现的接口，该接口需要实现 IServiceProvider
        val serviceProviderName = (this as KSClassDeclaration).superTypes
            .map(KSTypeReference::resolve)
            .find { superType ->
                (superType.declaration as? KSClassDeclaration)?.superTypes?.any {
                    it.resolve().declaration.qualifiedName?.asString() == IServiceProvider::class.qualifiedName
                } == true
            }!!.declaration.qualifiedName?.asString()!!
        item.serviceProviderName = serviceProviderName
        item.serviceImplName = this.qualifiedName?.asString()!!
    }
    logger.info("[extractServiceMeta] parse result: $item")
    return item
}

