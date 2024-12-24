package cn.li.router.runtime


/**
 * 将 url 解析为 base_url 和 params 两部分
 * @author suweikai 2024/12/21
 */
class RouteUrl internal constructor(
    val originalUrl: String
) {
    val baseUrl: String
    private val _params = HashMap<String, String>()
    val params: Map<String, String> = _params

    init {
        val splitIdx = originalUrl.indexOf('?')
        if (splitIdx == -1) {
            // 没有分隔符
            baseUrl = originalUrl
        } else {
            baseUrl = originalUrl.substring(0, splitIdx)
            // 处理 ? 后面的参数
            originalUrl.substring(splitIdx + 1)
                .split('&')
                .forEach { pair ->
                    if (pair.isBlank()) {
                        return@forEach
                    }
                    val keyValue = pair.split("=")
                    // 非法的参数 xxx&yyy
                    if (keyValue.size != 2) {
                        throw IllegalArgumentException("`${originalUrl}` is illegal url!")
                    }
                    val (key, value) = keyValue
                    _params[key] = value
            }
        }
    }


    override fun toString(): String {
        val paramsString = StringBuilder()
        params.forEach { entry ->
            paramsString.append("[${entry.key}=${entry.value}]")
        }
        if (paramsString.isEmpty()) {
            paramsString.append("[empty]")
        }
        return "RouteUrl(originalUrl=${originalUrl}, baseUrl=${baseUrl}, params=$paramsString)"
    }

    fun valueOf(url: String): RouteUrl? {
        return try {
            RouteUrl(url)
        } catch (_: Exception) {
            null
        }
    }



}