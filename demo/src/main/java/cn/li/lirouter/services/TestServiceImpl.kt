package cn.li.lirouter.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import cn.li.router.api.annotations.Service

/**
 *
 * @author suweikai 2024/12/24
 */
//@Service(singleton = false)
class TestServiceImpl: ITestService {
    override fun test(context: Context) {
        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
    }
}

@Service(singleton = true, returnType = ITestService::class)
fun getTestService(): TestServiceImpl {
    return TestServiceImpl()
}


//fun main() {
//    ServiceProvider.getService(ITestService::class.java)?.test()
//}