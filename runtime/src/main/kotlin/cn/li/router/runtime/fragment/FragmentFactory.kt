package cn.li.router.runtime.fragment

import androidx.fragment.app.Fragment

/**
 *
 * @author suweikai 2024/12/22
 */
internal object FragmentFactory {

    private val classCaches = mutableMapOf<String, Class<*>>()


    @Suppress("UNCHECKED_CAST")
    fun <F: Fragment>createFragment(className: String): F? {
        synchronized(classCaches) {
            classCaches[className]?.let { clazz ->
                return clazz.getDeclaredConstructor().newInstance() as F
            }
            try {
                val clazz = Class.forName(className)
                if (clazz.isAssignableFrom(Fragment::class.java)) {
                    synchronized(classCaches) {
                        classCaches[className] = clazz
                    }
                    return clazz.getDeclaredConstructor().newInstance() as F
                } else {
                    throw IllegalArgumentException("$className isn't subclass of ${Fragment::class.java.canonicalName}")
                }
            } catch (_: Exception) {
                return null
            }
        }
    }


}