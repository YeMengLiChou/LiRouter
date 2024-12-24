package cn.li.lirouter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.li.lirouter.databinding.FragmentTestBinding
import cn.li.router.api.annotations.Autowired
import cn.li.router.api.annotations.Router

/**
 *
 * @author suweikai 2024/12/22
 */
@Router("lirouter://test")
class TestFragment: Fragment() {

    private val binding by lazy {
        FragmentTestBinding.inflate(layoutInflater)
    }

    @Autowired
    var a = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

}