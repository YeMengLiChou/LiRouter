package cn.li.lirouter.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.li.lirouter.databinding.ActivityLoginBinding
import cn.li.router.api.annotations.Router

/**
 *
 * @author suweikai 2024/12/24
 */

@Router(url = "lirouter://login")
class LoginActivity: AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}