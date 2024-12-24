package cn.li.router.runtime

import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import cn.li.router.LiRouter
import cn.li.router.runtime.callback.NavigationCallback
import java.io.Serializable
import java.lang.Exception

/**
 * 导航执行体，包含了导航所需的参数
 * @author suweikai 2024/12/21
 */
class RouteNavigator private constructor(builder: Builder) {
    companion object {
        private const val TAG = "${LiRouter.TAG}-Navigator"
    }

    val url = RouteUrl(builder.url)
    val context = builder.context
    val extras = builder.extras
    val activityOptions = builder.activityOptions
    val intentFlags = builder.flags
    @AnimRes
    val enterAnim = builder.enterAnim
    @AnimRes
    val exitAnim = builder.exitAnim
    private val logger get() =  LiRouter.logger

    /**
     * 跳转到指定的 Activity
     * */
    @Suppress("DEPRECATION")
    private fun navigate(callback: NavigationCallback? = null, shouldIntercept: Boolean = true) {
        // 拦截
        if (shouldIntercept) {
            val navigator = RouterController.processIntercept(this)
            if (navigator == null) {
                // 拦截返回 null，不处理本次跳转
                callback?.onRouteLost(this)
                logger.warn(TAG, "[navigate] intercept return null, abort navigate!")
                return
            } else if (navigator != this) {
                // 拦截返回新的 Navigate，重新处理
                navigator.navigate(callback, false)
                return
            }
        }

        // 找到对应的路由项
        val meta = RouterController.getRouteMeta(url)
        if (meta == null) {
            callback?.onRouteLost(this)
            logger.warn(TAG, "[navigate] not found meta: $url")
            return
        }

        // 检查是否正确使用
        if (!Activity::class.java.isAssignableFrom(meta.clazz)) {
            callback?.onRouteError(this, IllegalArgumentException("`$url` is not a Activity!"))
            logger.warn(TAG, "[navigate] `$url` is not a Activity")
            return
        }
        callback?.onRouteFound(this)

        val intent = Intent()
        // 指定跳转的 Activity
        intent.component = ComponentName(context.packageName, meta.clasName)
        // 将 url 带的参数加到 extras 中
        url.params.forEach { entry ->
            extras.putString(entry.key, entry.value)
        }
        // Navigator 携带的参数
        intent.putExtras(extras)

        intent.addFlags(intentFlags)

        if (context !is Activity) {
            // 非 Activity 跳转的，新开一个栈
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent, activityOptions?.toBundle())
        callback?.onRouteNavigate(this)

        // 过渡动画
        if (context is Activity && (enterAnim != 0 || exitAnim != 0)) {
            context.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    /**
     * 创建 Fragment
     * */
    @Suppress("UNCHECKED_CAST")
    fun <F: Fragment> createFragment(callback: NavigationCallback? = null, shouldIntercept: Boolean = true): F? {
        if (shouldIntercept) {
            val navigator = RouterController.processIntercept(this)
            if (navigator == null) {
                // 拦截返回 null，不处理本次跳转
                callback?.onRouteLost(this)
                logger.warn(TAG, "[navigate] intercept return null, abort navigate!")
                return null
            } else if (navigator != this) {
                return navigator.createFragment(callback, false)
            }
        }


        val meta = RouterController.getRouteMeta(url)
        if (meta == null) {
            callback?.onRouteLost(this)
            logger.warn(TAG, "[navigate] not found meta: $url")
            return null
        }
        callback?.onRouteFound(this)
        if (!Fragment::class.java.isAssignableFrom(meta.clazz)) {
            callback?.onRouteError(this, IllegalArgumentException("`$url` is not a Fragment!"))
            logger.warn(TAG, "[navigate] `$url` is not a Fragment")
            return null
        }
        val fragment: F = try {
            val constructor = meta.clazz.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance() as F
        } catch (e: Exception) {
            callback?.onRouteError(this, e)
            logger.warn(TAG, "[navigate] `${url}` create fragment instance error: ${e.message}")
            return null
        }
        callback?.onRouteNavigate(this)
        return fragment
    }



    class Builder(
        internal val context: Context,
        internal val url: String,
    ) {
        internal val extras = Bundle()
        internal var activityOptions: ActivityOptions? = null
        @AnimRes
        internal var enterAnim: Int = 0
        @AnimRes
        internal var exitAnim: Int = 0
        internal var flags: Int = 0

        fun withByte(key: String, value: Byte): Builder {
            extras.putByte(key, value)
            return this
        }

        fun withShort(key: String, value: Short): Builder {
            extras.putShort(key, value)
            return this
        }

        fun withInt(key: String, value: Int): Builder {
            extras.putInt(key, value)
            return this
        }

        fun withLong(key: String, value: Long): Builder {
            extras.putLong(key, value)
            return this
        }

        fun withBoolean(key: String, value: Boolean): Builder {
            extras.putBoolean(key, value)
            return this
        }

        fun withFloat(key: String, value: Float): Builder {
            extras.putFloat(key, value)
            return this
        }

        fun withDouble(key: String, value: Double): Builder {
            extras.putDouble(key, value)
            return this
        }

        fun withString(key: String, value: String?): Builder {
            extras.putString(key, value)
            return this
        }

        fun withSerializable(key: String, value: Serializable?): Builder {
            extras.putSerializable(key, value)
            return this
        }

        fun withParcelable(key: String, value: Parcelable?): Builder {
            extras.putParcelable(key, value)
            return this
        }

        fun withParcelableArray(key: String, value: Array<Parcelable>?): Builder {
            extras.putParcelableArray(key, value)
            return this
        }

        fun withActivityOptionsCompat(options: ActivityOptions?): Builder {
            activityOptions = options
            return this
        }

        fun withEnterAnim(@AnimRes id: Int): Builder {
            enterAnim = id
            return this
        }

        fun withExitAnim(@AnimRes id: Int): Builder {
            exitAnim = id
            return this
        }

        fun withFlags(flags: Int): Builder {
            this.flags = flags
            return this
        }

        fun addIntentFlags(flag: Int): Builder {
            flags = flags.or(flag)
            return this
        }

        fun build(): RouteNavigator {
            return RouteNavigator(this)
        }

        fun navigate(callback: NavigationCallback? = null) {
            RouteNavigator(this).navigate(callback)
        }
    }

}