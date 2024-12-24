package cn.li.router.compiler.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

/**
 *
 * @author suweikai 2024/12/17
 */

fun KSTypeReference.getQualifiedName(): String? {
    val resolvedType = this.resolve()
    return resolvedType.declaration.qualifiedName?.asString() ?: resolvedType.declaration.simpleName.asString()
}


inline fun <reified ANNOTATION> KSAnnotated.parseAnnotationInfo(onArgument: (name: String?, value: Any?) -> Unit) {
    annotations.find {
        it.shortName.asString() == ANNOTATION::class.java.simpleName
    }?.let { annotated ->
        annotated.arguments.forEach { argument ->
            onArgument(argument.name?.asString(), argument.value)
        }
    }
}

fun KSTypeReference.parseTypeString(): String {
    val type = resolve()
    val args = type.arguments
    val qualifiedName = type.declaration.qualifiedName?.asString()?: type.declaration.simpleName.asString()
    if (args.isEmpty()) {
        return qualifiedName
    } else {
        val typeStr = StringBuilder()
        typeStr.append(qualifiedName)
        typeStr.append('<')
        args.forEachIndexed { idx, arg ->
            if (arg.variance == Variance.STAR) {
                typeStr.append("*")
            } else {
                typeStr.append(arg.type?.parseTypeString() ?: "")
            }
            if (idx != args.lastIndex) {
                typeStr.append(", ")
            }
        }
        typeStr.append('>')
        return typeStr.toString()
    }
}