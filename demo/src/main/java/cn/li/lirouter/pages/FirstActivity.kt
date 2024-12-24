package cn.li.lirouter.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import cn.li.lirouter.databinding.ActivityFirstBinding
import cn.li.router.LiRouter
import cn.li.router.api.annotations.Autowired
import cn.li.router.api.annotations.Router

/**
 *
 * @author suweikai 2024/12/21
 */
@Router("lirouter://first")
class FirstActivity: Activity() {

    private val binding  by lazy {
        ActivityFirstBinding.inflate(layoutInflater)
    }
    @Autowired(names = ["string_arg"])
    var stringArg: String? = null

    @Autowired(names = ["int_arg"])
    var intArg: Int = 0

    @Autowired(names = ["long_arg"])
    var longArg: Long = 0L

    @Autowired(names = ["float_arg"])
    var floatArg: Float = 0f

    @Autowired(names = ["double_arg"])
    var doubleArg: Double = 0.0

    @Autowired(names = ["boolean_arg"])
    var booleanArg: Boolean = false

    @Autowired(names = ["pair_arg"])
    var pairArg: Pair<String, *>? = null

    @Autowired(names = ["parcelable_arg"])
    var parcelableArg: Bundle? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        LiRouter.autowire(this)
        binding.tvParams.text = """
            intArg: $intArg
            longArg: $longArg
            floatArg: $floatArg
            doubleArg: $doubleArg
            booleanArg: $booleanArg
            stringArg: $stringArg
            pairArg: $pairArg
            parcelableArg: ${parcelableArg?.get("1")}
        """.trimIndent()


    }
}