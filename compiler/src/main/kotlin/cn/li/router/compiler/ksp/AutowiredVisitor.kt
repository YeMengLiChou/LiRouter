package cn.li.router.compiler.ksp

import cn.li.router.api.annotations.Autowired
import cn.li.router.compiler.data.AutowiredMeta
import cn.li.router.compiler.utils.logger
import cn.li.router.compiler.utils.parseAnnotationInfo
import cn.li.router.compiler.utils.parseTypeString
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.visitor.KSDefaultVisitor

/**
 * 用来解析 [Autowired] 注解的信息
 * @author suweikai 2024/12/22
 */
open class AutowiredVisitor: KSDefaultVisitor<AutowiredMeta, Unit>() {

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: AutowiredMeta) {
        if (!property.annotations.any { it.shortName.asString() == Autowired::class.simpleName!! }) {
            return
        }
        if (!property.isMutable || property.isDelegated()) {
            logger.error("[RouterKSVisitor] `Autowired` annotated on a non-mutable or delegated field is not allowed!")
            return
        }
        val infoItem = AutowiredMeta.Item()
        infoItem.type = property.type.parseTypeString()
        infoItem.fieldName = property.simpleName.asString()
        property.parseAnnotationInfo<Autowired> { name, value ->
            when(name) {
                AutowiredMeta.NAMES -> {
                    infoItem.otherNames = (value as? ArrayList<*>)?.map { it as String }
                }
                AutowiredMeta.REQUIRED -> {
                    infoItem.required = (value as? Boolean)?: false
                }
            }
        }
        data.items.add(infoItem)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: AutowiredMeta) {
        super.visitClassDeclaration(classDeclaration, data)
        data.ownerQualifiedName = classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()
        classDeclaration.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .forEach {
                visitPropertyDeclaration(it, data)
            }
    }

    override fun defaultHandler(node: KSNode, data: AutowiredMeta) { }



}