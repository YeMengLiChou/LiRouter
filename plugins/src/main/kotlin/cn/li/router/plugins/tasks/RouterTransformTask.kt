package cn.li.router.plugins.tasks

import cn.li.router.plugins.asm.RouterAsmVisitor
import cn.li.router.plugins.utils.AsmConstants
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.math.log

/**
 *
 * @author suweikai 2024/12/19
 */
abstract class RouterTransformTask: DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirs: ListProperty<Directory>

    @get:OutputFile
    abstract val outputJar: RegularFileProperty


    @Internal
    val jarPaths = mutableSetOf<String>()

    @Internal
    val spiServices = mutableSetOf<String>()

    @TaskAction
    fun transformAction() {

        var injectJar: JarFile? = null
        val modules = mutableSetOf<String>()
        val output = JarOutputStream(outputJar.get().asFile.outputStream())

        allJars.get().forEach { jar ->
            val jarFile = JarFile(jar.asFile)
            jarFile.entries()
                .asSequence()
                .filter { !it.isDirectory }
                .forEach entryForeach@ { entry ->
                    if (entry.name == AsmConstants.ROUTER_INJECT_CLASS_NAME) {
                        logger.info("[LiRouterPlugin] Jar中找到用于注入的 cn.li.router.RouterInject 类")
                        injectJar = jarFile
                        return@entryForeach
                    } else if (entry.name.startsWith(AsmConstants.ROUTER_MODULE_CLASS_NAME_PREFIX)) {
                        // 收集所有生成的 module
                        val qualifiedName = entry.name.removeSuffix(".class")
                        modules.add(qualifiedName)
                        logger.info("[LiRouterPlugin] Jar中找到生成类: $qualifiedName")
                    }

                    // 将这些类放入 output 中
                    output.writeNextEntry(entry.name, jarFile.getInputStream(entry))
                }
        }

        allDirs.get().forEach { dir ->
            val dirFile = dir.asFile
            val dirUri = dirFile.toURI()
            dirFile
                .walk()
                .filter(File::isFile)
                .forEach fileForeach@ { file ->
                    // 拿到相对路径部分，作为 JarEntry.name
                    val entryName = dirUri.relativize(file.toURI()).path.replace(File.separatorChar, '/')
                    if (entryName == AsmConstants.ROUTER_INJECT_CLASS_NAME) {
                        logger.info("[LiRouterPlugin] Dir中找到用于注入的 cn.li.router.RouterInject 类")
                        injectJar = JarFile(file)
                        return@fileForeach
                    } else if (entryName.startsWith(AsmConstants.ROUTER_MODULE_CLASS_NAME_PREFIX)) {
                        // 收集所有生成的 module
                        val qualifiedName = entryName.removeSuffix(".class")
                        modules.add(qualifiedName)
                        logger.info("[LiRouterPlugin] Dir中找到生成类: $qualifiedName")
                    }
                    // 将这些类放入 output 中
                    output.writeNextEntry(entryName, file.inputStream())
                }
        }

        if (injectJar == null) {
            throw RuntimeException("[LiRouterPlugin] 未找到用于注入的 cn.li.router.RouterInject 类")
        }
        // 拿到注入类，经过 asm 插桩后，写入 output 中
        output.putNextEntry(JarEntry(AsmConstants.ROUTER_INJECT_CLASS_NAME))
        injectJar!!.getInputStream(injectJar!!.getEntry(AsmConstants.ROUTER_INJECT_CLASS_NAME)).use { input ->
            // reader -> visitor -> writer
            val classReader = ClassReader(input)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val classVisitor = RouterAsmVisitor(modules, classWriter)
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG)
            output.write(classWriter.toByteArray())
            output.closeEntry()
        }

        // 写入 spi 服务
        if (spiServices.isNotEmpty()) {
            output.putNextEntry(JarEntry(AsmConstants.SPI_SERVICE_CLASS_NAME))
            output.write(spiServices.joinToString("\n").toByteArray())
            output.closeEntry()
        }
        injectJar?.close()
        output.close()
    }

    private fun JarOutputStream.writeNextEntry(name: String, inputStream: InputStream) {
        if (isLiRouterModuleSPI(name)) {
            // 合并 spi 服务
            inputStream.use { ins ->
                spiServices.addAll(ins.readBytes().decodeToString().split("\n").filter { it.isNotEmpty() })
            }
            return
        }
        if (jarPaths.contains(name) ) {
            return
        }
        jarPaths.add(name)
        putNextEntry(JarEntry(name))
        inputStream.copyTo(this)
        inputStream.close()
        closeEntry()
    }

    private fun isLiRouterModuleSPI(name: String): Boolean {
        return name == AsmConstants.SPI_SERVICE_CLASS_NAME
    }

}