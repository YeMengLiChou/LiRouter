package cn.li.router.runtime

import android.app.Activity
import androidx.fragment.app.Fragment
import cn.li.router.LiRouter
import cn.li.router.runtime.meta.AutowiredMeta


object AutowiredArgumentsParser {

     @Suppress("DEPRECATION")
     inline fun <reified T> parse(target: Any, meta: AutowiredMeta): T? {
        val args = when (target) {
            is Activity -> target.intent.extras
            is Fragment -> target.arguments
            is android.app.Fragment -> target.arguments
            else -> null
        }
        val clazz = T::class.java
        meta.names.forEach { key ->
            var value: Any? = args?.get(key) ?: return@forEach
            // string 类型的值可能是 url 携带的参数，需要处理下
            if (value is String) {
                if (clazz.isPrimitive || clazz.isWrapperPrimitive()) {
                    value = value.toPrimitive(clazz)
                }
                if (clazz != String::class.java) {
                    if (LiRouter.jsonMapper == null) {
                        throw IllegalStateException("jsonMapper is null, please call `LiRouter.setJsonMapper()` first")
                    }
                    value = LiRouter.jsonMapper!!.fromJson(value as String, clazz)
                }
            }
            return value as? T
        }
        if (meta.required) {
            throw IllegalArgumentException("the required argument is null!")
        }
        return null
    }

    fun String.toPrimitive(clazz: Class<*>): Any? {
        val name = clazz.kotlin.qualifiedName
        return try {
            when(name) {
                "kotlin.Byte" -> this.toByte()
                "kotlin.Short" -> this.toShort()
                "kotlin.Int" -> this.toInt()
                "kotlin.Long" -> this.toFloat()
                "kotlin.Float" -> this.toFloat()
                "kotlin.Double" -> this.toDouble()
                "kotlin.Boolean" -> this.toBoolean()
                "kotlin.Char" -> this.toCharArray()
                else -> this
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 是否为基本类型包装类
     * */
    fun <T> Class<T>.isWrapperPrimitive(): Boolean {
        return this == java.lang.Byte::class.java ||
                this == java.lang.Short::class.java ||
                this == java.lang.Integer::class.java ||
                this == java.lang.Long::class.java ||
                this == java.lang.Float::class.java ||
                this == java.lang.Double::class.java ||
                this == java.lang.Boolean::class.java ||
                this == java.lang.Character::class.java
    }
}