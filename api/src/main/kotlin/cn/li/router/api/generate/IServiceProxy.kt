package cn.li.router.api.generate

import cn.li.router.api.interfaces.IServiceProvider

/**
 *
 * @author suweikai 2024/12/24
 */
interface IServiceProxy {

    fun createService(): IServiceProvider

}