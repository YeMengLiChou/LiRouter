
package cn.li.router.compiler.utils

import cn.li.router.compiler.data.ServiceMeta
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

object RouterChecker {
    lateinit var logger: KSPLogger
    fun checkRouterAnnotation(annotated: KSAnnotated): Boolean {
        logger.info("[checkRouterAnnotation] check: $annotated")
         // 注解不在类上
        if (annotated !is KSClassDeclaration) {
            logger.warn("[checkRouterAnnotation] annotation is not on class declaration: $annotated")
            return false
        }
        // 抽象类
        if (annotated.modifiers.contains(Modifier.ABSTRACT)) {
            logger.warn("[checkRouterAnnotation] annotation is on abstract class declaration: $annotated")
            return false
        }
        // 检查是否为实现类（递归resolve开销较大）
//        val isSubclassOfActivityOrFragment =
//            annotated.getAllSuperTypes().any { superType ->
//                val qualifiedName = superType.declaration.qualifiedName?.asString()
//                qualifiedName == Constants.ACTIVITY_QUALIFIED_NAME || qualifiedName == Constants.FRAGMENT_QUALIFIED_NAME || qualifiedName == Constants.FRAGMENT_X_QUALIFIED_NAME
//            }
//        if (!isSubclassOfActivityOrFragment) {
//            logger.warn("[checkRouterAnnotation] annotation is on class declaration, but not subclass of Activity or Fragment: $annotated")
//            return false
//        }
        return true
    }

    fun checkInterceptorAnnotation(annotated: KSAnnotated): Boolean {
        logger.info("[checkInterceptorAnnotation] check: $annotated")
         // 注解不在类上
        if (annotated !is KSClassDeclaration) {
            logger.warn("[checkInterceptorAnnotation] annotation is not on class declaration: $annotated")
            return false
        }
        // 抽象类
        if (annotated.modifiers.contains(Modifier.ABSTRACT)) {
            logger.warn("[checkInterceptorAnnotation] annotation is on abstract class declaration: $annotated")
            return false
        }
        val isImplementationOfInterceptor = annotated.getAllSuperTypes().any {
            it.declaration.qualifiedName?.asString() == Constants.ROUTE_INTERCEPTOR_QUALIFIED_NAME
        }
        if (!isImplementationOfInterceptor) {
            logger.warn("[checkInterceptorAnnotation] annotation is on class declaration, but not subclass of IRouteInterceptor: $annotated")
            return false
        }
        return true
    }

    fun checkServiceImplAnnotation(annotated: KSAnnotated): Boolean {
        logger.info("[checkServiceImplAnnotation] check: $annotated")
        // 注解不在类或方法上
        if (annotated !is KSClassDeclaration && annotated !is KSFunctionDeclaration) {
            logger.warn("[checkServiceImplAnnotation] annotation is not on class declaration or func declaration: $annotated")
            return false
        }

        if (annotated is KSClassDeclaration) {
            // 抽象类
            if (annotated.modifiers.contains(Modifier.ABSTRACT)) {
                logger.warn("[checkServiceImplAnnotation] annotation is on abstract class declaration: $annotated")
                return false
            }
            if (annotated.primaryConstructor?.parameters?.isNotEmpty() == true) {
                logger.warn("[checkServiceImplAnnotation] annotation is on a class with non-empty constructor: $annotated")
                return false
            }

            // 检查类是否实现了 IServiceProvider 接口
            val isImplementation = annotated.getAllSuperTypes().any {
                it.declaration.qualifiedName?.asString() == Constants.SERVICE_IMPL_QUALIFIED_NAME
            }
            if (!isImplementation) {
                logger.warn("[checkServiceImplAnnotation] annotated class not implement `IServiceProvider`: $annotated")
                return false
            }
            return true
        }

        (annotated as KSFunctionDeclaration).let { func ->
            // 抽象方法
            val isAbstract = func.modifiers.contains(Modifier.ABSTRACT)
            val isPrivate = func.modifiers.contains(Modifier.PRIVATE)
            val isInternal = func.modifiers.contains(Modifier.INTERNAL)
            if (isAbstract || isPrivate || isInternal) {
                logger.warn("[checkServiceImplAnnotation] annotation is on illegal access func declaration: $annotated")
                return false
            }
            // 非无参方法
            if (func.parameters.isNotEmpty()) {
                logger.warn("[checkServiceImplAnnotation] annotation is on func declaration, but has params: $annotated")
                return false
            }
            if (func.functionKind != FunctionKind.STATIC && func.functionKind != FunctionKind.TOP_LEVEL) {
                logger.warn("[checkServiceImplAnnotation] annotation is on func declaration, but not static or top level: $annotated")
                return false
            }
            return true
        }
    }


    /**
     * 检查是否有重复的 Service 实现类
     * */
    fun checkDistinctService(metas: List<ServiceMeta>): Boolean {
        val map = mutableMapOf<String, ServiceMeta>()
        metas.forEach { meta ->
            val key = meta.serviceProviderName
            if (map.containsKey(key)) {
                logger.error("[checkDistinctService] ${meta.serviceProviderName} 存在多个实现类: ${map[key]?.serviceImplName}, ${meta.serviceImplName}")
                return false
            }
            map[key] = meta
        }
        return true
    }
}