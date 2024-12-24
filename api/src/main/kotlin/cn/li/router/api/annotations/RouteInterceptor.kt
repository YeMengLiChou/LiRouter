package cn.li.router.api.annotations

/**
 *
 * @author suweikai 2024/12/24
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RouteInterceptor(
    val priority: Int = 5
)