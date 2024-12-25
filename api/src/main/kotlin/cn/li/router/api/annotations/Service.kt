package cn.li.router.api.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Service(
    val singleton: Boolean = false, // 是否为单例
    val returnType: KClass<*> = Unit::class, // 返回类型
)
