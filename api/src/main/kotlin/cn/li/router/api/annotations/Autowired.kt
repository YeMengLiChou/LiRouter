package cn.li.router.api.annotations

/**
 * 路由跳转时标识需要自动注入的参数
 *
 * */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class Autowired(
    /**
     * 按顺序匹配的别名，为空时默认使用字段名称，
     * */
    val names: Array<String> = [],

    /**
     * 路由跳转是否必须携带该参数，当没有携带(true)时或者解析失败时会抛出异常
     * */
    val required: Boolean = false
)
