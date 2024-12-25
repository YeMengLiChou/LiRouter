package cn.li.lirouter.services

import android.content.Context
import cn.li.router.api.interfaces.IServiceProvider

/**
 *
 * @author suweikai 2024/12/24
 */
interface ITestService: IServiceProvider {

    fun test(context: Context)

}