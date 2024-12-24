package cn.li.router.runtime.meta


/**
 *
 * @author suweikai 2024/12/17
 */
data class RouteMeta(
    val url: String,
    val desc: String,
    val clasName: String,
    val clazz: Class<*>
)