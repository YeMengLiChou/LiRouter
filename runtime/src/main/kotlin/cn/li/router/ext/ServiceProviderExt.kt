package cn.li.router.ext

import cn.li.router.ServiceProvider
import cn.li.router.api.interfaces.IServiceProvider
import kotlin.reflect.KClass

/**
 *
 * @author suweikai 2024/12/25
 */

fun <T: IServiceProvider> KClass<T>.impl(): T? {
    return ServiceProvider.getService(this.java)
}

fun <T: IServiceProvider> Class<T>.impl(): T? {
    return ServiceProvider.getService(this)
}

