package cn.li.router.runtime.logger

/**
 * 日志接口
 * @author suweikai 2024/12/21
 */
interface IRouterLogger {

    fun debug(tag: String, format: String, vararg args: String)

    fun info(tag: String, format: String, vararg args: String)

    fun warn(tag: String, format: String, vararg args: String)

    fun error(tag: String, format: String, throwable: Throwable? = null,  vararg args: String)
}