package cn.li.router.compiler.ksp

import cn.li.router.api.annotations.RouteInterceptor
import cn.li.router.api.annotations.Router
import cn.li.router.api.annotations.Service
import cn.li.router.compiler.data.AutowiredMeta
import cn.li.router.compiler.data.RouteInterceptorMeta
import cn.li.router.compiler.data.RouteMeta
import cn.li.router.compiler.data.RouterModule
import cn.li.router.compiler.data.ServiceMeta
import cn.li.router.compiler.utils.Constants
import cn.li.router.compiler.utils.RouterChecker
import cn.li.router.compiler.utils.extractAutowiredMeta
import cn.li.router.compiler.utils.extractRouteInterceptorMeta
import cn.li.router.compiler.utils.extractRouteMeta
import cn.li.router.compiler.utils.extractServiceMeta
import cn.li.router.compiler.utils.genIAutowiredImpl
import cn.li.router.compiler.utils.genServiceProxy
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import java.io.BufferedWriter
import cn.li.router.compiler.utils.logger as utilsLogger


/**
 * 解析出所有的内容，生成对应的代码
 * @author suweikai 2024/12/17
 */
class LiRouterProcessor(
    environment: SymbolProcessorEnvironment
): SymbolProcessor {

    companion object {
        val RouterQualifiedName = Router::class.qualifiedName!!
    }

    private val logger = environment.logger.also {
        RouterChecker.logger = it
        utilsLogger = it
    }


    private val codeGenerator = environment.codeGenerator

    private val routerKsFiles = mutableSetOf<KSFile>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("LiRouterProcessor start")
        routerKsFiles.clear()
        processRouter(resolver)
        return emptyList()
    }

    private fun processRouter(resolver: Resolver) {
        val routerSymbols = resolver.getSymbolsWithAnnotation(RouterQualifiedName)
        logger.info("routerSymbols: ${routerSymbols.toList()}")

        val routers = routerSymbols
            .filter(RouterChecker::checkRouterAnnotation) // 过滤出符合条件的
            .onEach { it.containingFile?.let(routerKsFiles::add) } // 收集生成类所依赖的文件
            .toList()

        // router 注解信息
        val routerInfoItems = routers
            .map(KSAnnotated::extractRouteMeta)
            .sortedBy { it.url }

        // autowired 注解信息
        val autowiredMetas = routers
            .map(KSAnnotated::extractAutowiredMeta)
            .filter {
                it.items.isNotEmpty()
            }
            .onEach { meta ->
                // 生成 IAutowired 实现类
                meta.genIAutowiredImpl(codeGenerator, logger)
            }

        // RouteInterceptor 信息
        val interceptorMetas =
            resolver.getSymbolsWithAnnotation(RouteInterceptor::class.qualifiedName!!)
                .filter(RouterChecker::checkInterceptorAnnotation)
                .onEach { it.containingFile?.let(routerKsFiles::add)  }
                .map(KSAnnotated::extractRouteInterceptorMeta)
                .toList()

        // Service 信息
        val serviceMetas = resolver.getSymbolsWithAnnotation(Service::class.qualifiedName!!)
            .filter(RouterChecker::checkServiceImplAnnotation)
            .onEach { it.containingFile?.let(routerKsFiles::add)  }
            .map(KSAnnotated::extractServiceMeta)
            .toList()
            .takeIf(RouterChecker::checkDistinctService)
            ?.onEach { meta ->
                // 生成代理类
                meta.genServiceProxy(codeGenerator, logger)
            }

        serviceMetas ?: return

        logger.info("routerInfoItems: $routerInfoItems")
        logger.info("autowiredMetas: $autowiredMetas")
        logger.info("interceptors: $interceptorMetas")
        logger.info("serviceMetas: $serviceMetas")

        // 生成带有收集到的信息的类
        generateRouterModuleCode(
            RouterModule(routerInfoItems, autowiredMetas, interceptorMetas, serviceMetas)
        )
    }

    /**
     * 生成路由类注册代码类
     * */
    private fun generateRouterModuleCode(module: RouterModule) {
        logger.info("[generateRouterCode] start: ${module.routers.size} routers, ${module.autowiredMetas.size} autowireds, ${module.interceptorMetas.size} interceptors")
        if (module.routers.isEmpty() && module.autowiredMetas.isEmpty() && module.interceptorMetas.isEmpty()) {
            return
        }
        val clasName = "%s%d".format(Constants.PREFIX_ROUTER_MAP, System.currentTimeMillis())
        val dependencies = Dependencies(true, *routerKsFiles.toTypedArray())
        codeGenerator.createNewFile(dependencies, Constants.GENERATE_PACKAGE_NAME, clasName, "kt")
            .bufferedWriter(Charsets.UTF_8)
            .use { writer ->
                writer.appendLine("package ${Constants.GENERATE_PACKAGE_NAME}")
                writer.appendLine()
                writer.appendLine("/* The file auto generated by LiRouter/Grimrise. Don't edit it! */")
                writer.appendLine("class $clasName: cn.li.router.api.generate.IRouterModule {")

                generateInitRouteCode(writer, module.routers)
                generateInitAutowiredCode(writer, module.autowiredMetas)
                generateInitInterceptorCode(writer, module.interceptorMetas)
                generateInitServiceProviderCode(writer, module.serviceMetas)

                writer.appendLine("}")
            }

        // 将生成的类注册到SPI中
        codeGenerator.createNewFileByPath(dependencies, "META-INF/services/cn.li.router.api.generate.IRouterModule", "")
            .bufferedWriter(Charsets.UTF_8)
            .use { writer ->
                writer.appendLine("${Constants.GENERATE_PACKAGE_NAME}.$clasName")
            }
    }

    private fun generateInitRouteCode(writer: BufferedWriter, routerItems: List<RouteMeta>) {
        writer.appendLine("\toverride fun initRouter() {")
        routerItems.forEach { item ->
            writer.appendLine("\t\tcn.li.router.runtime.RouterController.addRouteMeta(${item.toRouteMetaString()})")
        }
        writer.appendLine("\t}")
    }

    private fun generateInitAutowiredCode(writer: BufferedWriter, metas: List<AutowiredMeta>) {
        writer.appendLine("\toverride fun initAutowired() {")
        metas.forEach { meta ->
            writer.appendLine("\t\tcn.li.router.runtime.RouterController.registerAutowired(${meta.ownerQualifiedName}::class.java, cn.li.router.generate.${meta.getGenerateClassFileName()}())")
        }
        writer.appendLine("\t}")
    }

    private fun generateInitInterceptorCode(writer: BufferedWriter, metas: List<RouteInterceptorMeta>) {
        writer.appendLine("\toverride fun initRouteInterceptor() {")
        metas.forEach { meta ->
            writer.appendLine("\t\tcn.li.router.runtime.RouterController.registerInterceptor(${meta.ownerQualifiedName}(), ${meta.priority})")
        }
        writer.appendLine("\t}")
    }

    private fun generateInitServiceProviderCode(writer: BufferedWriter, metas: List<ServiceMeta>) {
        writer.appendLine("\toverride fun initServiceProvider() {")
        metas.forEach { meta ->
            writer.appendLine("\t\tcn.li.router.ServiceProvider.registerService(${meta.serviceProviderName}::class.java, ${meta.getGenerateClassFileName()}())")
        }
        writer.appendLine("\t}")
    }
}