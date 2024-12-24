package cn.li.router.plugins.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 *
 * @author suweikai 2024/12/17
 */
class RouterAsmVisitor(
    private val allModules: Set<String>,
    nextClassVisitor: ClassVisitor
): ClassVisitor(Opcodes.ASM9, nextClassVisitor) {


    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (name == "initLiRouter") {
            println("[LiRouter] visitMethod: ${name}")
            return RouterMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions), access, name, descriptor)
        }
        if (name == "initAutowire") {
            println("[LiRouter] visitMethod: ${name}")
            return RouterMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions), access, name, descriptor)
        }
        if (name == "initRouteInterceptor") {
            println("[LiRouter] visitMethod: ${name}")
            return RouterMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions), access, name, descriptor)
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }


    inner class RouterMethodVisitor(api: Int, nextMethodVisitor: MethodVisitor, access: Int, name: String?, descriptor: String?):
        AdviceAdapter(api, nextMethodVisitor, access, name, descriptor) {

        override fun onMethodEnter() {
            if (name == "initLiRouter") {
                allModules.forEach { moduleName ->
                    println("[LiRouter] insertModule: $moduleName")
                    moduleName.createModuleInstance()
                    moduleName.invokeModuleMethod("initRouter", "()V")
                }
            }
            if (name == "initAutowire") {
                allModules.forEach { moduleName ->
                    println("[LiRouter] insertModule: $moduleName")
                    moduleName.createModuleInstance()
                    moduleName.invokeModuleMethod("initAutowired", "()V")
                }
            }

            if (name == "initRouteInterceptor") {
                allModules.forEach { moduleName ->
                    println("[LiRouter] insertModule: $moduleName")
                    moduleName.createModuleInstance()
                    moduleName.invokeModuleMethod("initRouteInterceptor", "()V")
                }
            }
        }

        private fun String.createModuleInstance() {
            mv.visitTypeInsn(Opcodes.NEW, this)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this, "<init>", "()V", false)
        }

        private fun String.invokeModuleMethod(name: String, descriptor: String) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this, name, descriptor, true)
        }

    }
}