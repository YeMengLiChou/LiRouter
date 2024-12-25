package cn.li.router

import cn.li.router.LiRouter.logger
import cn.li.router.api.generate.IServiceProxy
import cn.li.router.api.interfaces.IServiceProvider

/**
 * provider <- impl
 *
 * */
object ServiceProvider {
    private const val TAG = "ServiceProvider"
    private val serviceProxies = HashMap<Class<*>, IServiceProxy>()

    fun registerService(clazz: Class<*>, proxy: IServiceProxy) {
        synchronized(serviceProxies) {
            serviceProxies[clazz] = proxy
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: IServiceProvider> getService(clazz: Class<T>): T? {
        synchronized(serviceProxies) {
            if (serviceProxies.contains(clazz)) {
                return serviceProxies[clazz]?.createService() as? T
            } else {
                return tryGetByReflect(clazz)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: IServiceProvider> tryGetByReflect(clazz: Class<T>): T? {
        val serviceProxyName = clazz.canonicalName?.replace('.', '_')!! + "__LiRouter__ServiceProxy"
        try {
            val serviceProxyClass = Class.forName(serviceProxyName)
            val constructor = serviceProxyClass.getDeclaredConstructor()
            constructor.isAccessible = true
            val proxy =  constructor.newInstance() as IServiceProxy
            registerService(clazz, proxy)
            return proxy.createService() as? T
        } catch (e: Exception) {
            logger.info(TAG, "[tryGetByReflect] error: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}