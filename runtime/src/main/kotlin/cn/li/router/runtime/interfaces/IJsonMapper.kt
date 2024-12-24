package cn.li.router.runtime.interfaces

/**
 *
 * @author suweikai 2024/12/24
 */
interface IJsonMapper {

    fun <T> toJson(value: T): String

    fun <T> fromJson(json: String, clazz: Class<T>): T
}