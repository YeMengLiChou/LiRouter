package cn.li.lirouter.pages

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import cn.li.lirouter.databinding.ActivityMainBinding
import cn.li.router.LiRouter
import cn.li.router.api.annotations.Router
import cn.li.router.runtime.RouteNavigator
import cn.li.router.runtime.callback.NavigationCallback

/**
 *
 * @author suweikai 2024/12/17
 */
@Router(url = "lirouter://main")
class MainActivity: AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        LiRouter.init()
        initViews()
    }

    private fun initViews() {
        binding.btNavigate.setOnClickListener {
            val url = binding.etUrlInput.text.toString()
            if (url.startsWith("lirouter://")) {
                navigateTo(url)
            } else {
                Toast.makeText(this, "illegal url", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btCreateFragment.setOnClickListener {
            val url = binding.etFragmentUrlInput.text.toString()
            if (url.startsWith("lirouter://")) {
                val fragment = LiRouter.route(this, url)
                    .build()
                    .createFragment<Fragment>(null)
                Toast.makeText(this, "fragment created:${fragment}", Toast.LENGTH_SHORT).show()
                fragment?.let {
                    supportFragmentManager.commit {
                        add(binding.fragmentContainer, it, null)
                    }
                }
            } else {
                Toast.makeText(this, "illegal url", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun navigateTo(url: String) {
        LiRouter
            .route(this, url)
            .withInt("int_arg", 1)
            .withShort("short_arg", 2)
            .withLong("long_arg", 3)
            .withFloat("float_arg", 4.0f)
            .withDouble("double_arg", 5.0)
            .withBoolean("boolean_arg", true)
            .withString("string_arg", "123")
            .withSerializable("pair_arg", Pair("12", 1))
            .withParcelable("parcelable_arg", Bundle().apply { putChar("1", '1') })
            .navigate(object : NavigationCallback {
                override fun onRouteLost(navigator: RouteNavigator) {
                    Toast.makeText(this@MainActivity, "not found", Toast.LENGTH_SHORT).show()
                }

                override fun onRouteError(navigator: RouteNavigator, throwable: Throwable) {

                }

                override fun onRouteFound(navigator: RouteNavigator) {

                }

                override fun onRouteNavigate(navigator: RouteNavigator) {
                    Toast.makeText(this@MainActivity, "navigated", Toast.LENGTH_SHORT).show()
                }
            })
    }

}