package cn.li.router.plugins.transform

import cn.li.router.plugins.asm.RouterAsmVisitor
import cn.li.router.plugins.utils.AsmConstants
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor

/**
 *
 * @author suweikai 2024/12/17
 */
abstract class RouterAsmClassVisitorFactory: AsmClassVisitorFactory<InstrumentationParameters.None> {


}