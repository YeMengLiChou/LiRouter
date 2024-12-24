package cn.li.router.api.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Router(
    val url: String,
    val desc: String = ""
)
