
package cn.li.router.compiler.utils

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
}