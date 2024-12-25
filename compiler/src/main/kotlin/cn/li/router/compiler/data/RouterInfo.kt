package cn.li.router.compiler.data

import cn.li.router.compiler.utils.Constants
import com.google.devtools.ksp.symbol.KSFile
import kotlin.reflect.KClass

/**
 *
 * @author suweikai 2024/12/22
 */
sealed interface RouterInfo

class RouteMeta: RouterInfo {
    var url: String? = null
    var desc: String? = null
    var clasName: String? = null

    companion object {
        const val URL = "url"
        const val DESC = "desc"
    }

    override fun toString(): String {
        return "RouteMeta(url='$url', desc='$desc', ownerQualifiedName='$clasName')"
    }

    fun toRouteMetaString(): String {
        return "cn.li.router.runtime.meta.RouteMeta(\"${url}\", \"${desc}\", \"${clasName}\", ${clasName}::class.java)"
    }
}

class AutowiredMeta: RouterInfo {
    var ownerQualifiedName: String = ""
    val items = mutableListOf<Item>()

    companion object {
        const val NAMES = "names"
        const val REQUIRED = "required"
    }

    fun getGenerateClassFileName(): String {
        return "${ownerQualifiedName.replace('.', '_')}${Constants.SUFFIX_ROUTER_AUTOWIRED_IMPL}"
    }

    override fun toString(): String {
        return "AutowiredMeta(ownerQualifiedName='$ownerQualifiedName', items=\n${
            items.joinToString(
                separator = "\n\t\t"
            )
        })"
    }

    class Item {
        var type: String = ""
        var fieldName: String = ""
        var otherNames: List<String>? = null
        var required: Boolean = false

        fun toAutowiredMetaString(): String {
            val names = if (otherNames.isNullOrEmpty()) {
                listOf(fieldName)
            } else {
                otherNames!!
            }
            return "cn.li.router.runtime.meta.AutowiredMeta(listOf(${names.joinToString(separator = ",") { "\"$it\"" }}), ${required})"
        }

        override fun toString(): String {
            return "Item(type='$type', fieldName='$fieldName', otherNames=$otherNames, required=$required)"
        }
    }

}

class RouteInterceptorMeta: RouterInfo {
    var priority: Int = 0
    var ownerQualifiedName: String = ""


    override fun toString(): String {
        return "RouteInterceptorMeta(priority=$priority, ownerQualifiedName='$ownerQualifiedName')"
    }

    companion object {
        const val PRIORITY = "priority"
    }
}

class ServiceMeta: RouterInfo {
    var isSingleton: Boolean = false
    var serviceProviderName: String = ""
    var serviceImplName: String = ""
    var isFunction: Boolean = false
    var isObject: Boolean = false
    var returnType: String = ""
    var ksFile: KSFile? = null

    override fun toString(): String {
        return "ServiceMeta(isSingleton=$isSingleton, serviceProviderName='$serviceProviderName', serviceImplName='$serviceImplName', isFunction=$isFunction, returnType=$returnType)"
    }

    fun getGenerateClassFileName(): String {
        return "${serviceProviderName.replace('.', '_')}${Constants.SUFFIX_ROUTER_SERVICE_PROXY}"
    }

    companion object {
        const val SINGLETON = "singleton"
        const val RETURN_TYPE = "returnType"
    }
}