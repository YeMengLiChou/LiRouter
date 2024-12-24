package cn.li.router.runtime.logger

import android.util.Log

/**
 * 默认日志
 * @author suweikai 2024/12/22
 */
internal object DefaultRouterLogger: IRouterLogger {
    override fun debug(tag: String, format: String, vararg args: String) {
        Log.d(tag, formatMsg(format, *args))
    }

    override fun info(tag: String, format: String, vararg args: String) {
        Log.i(tag, formatMsg(format, *args))
    }

    override fun warn(tag: String, format: String, vararg args: String) {
        Log.i(tag, formatMsg(format, *args))
    }

    override fun error(
        tag: String,
        format: String,
        throwable: Throwable?,
        vararg args: String
    ) {
        Log.e(tag, formatMsg(format, *args), throwable)
    }

    private fun formatMsg(format: String, vararg args: String): String {
        return format.format(args)
    }
}