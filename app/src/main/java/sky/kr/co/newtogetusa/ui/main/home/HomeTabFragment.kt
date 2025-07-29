package sky.kr.co.newtogetusa.ui.main.home

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHomeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeRegisteredAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.ApplyAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.AvailableAdapter

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeBinding, HomeTabViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_home
    override val viewModel: HomeTabViewModel by viewModels()

    private var prgAdapter: HomeProgressAdapter? = null
    private var regAdapter: HomeRegisteredAdapter? = null
    private var applyAdapter : ApplyAdapter? = null
    private var availableAdapter : AvailableAdapter? = null

    override fun init() {
        super.init()

        prgAdapter = HomeProgressAdapter()
        dataBinding.rvProgress.apply {
            adapter = prgAdapter
        }
        regAdapter = HomeRegisteredAdapter().apply {
            setItems(listOf("1", "2","3"))
        }
        dataBinding.rvRegistered.apply {
            adapter = regAdapter
        }

        applyAdapter = ApplyAdapter().apply {
            setItems(listOf("1", "2","3"))
        }
        dataBinding.rvApply.apply {
            adapter = applyAdapter
        }

        availableAdapter = AvailableAdapter().apply {
            setItems(listOf("1", "2","3"))
        }
        dataBinding.rvAvailable.apply {
            adapter = availableAdapter
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.isModePlayer.collectLatest {

                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                HomeTabViewModel.Event.JoinPlayer ->{
                    findNavController().navigate(R.id.action_homeTabFragment_to_playerJoinFragment)
                }
            }
        }


        /*dataBinding.tvDelivery.setOnClickListener {
            findNavController().navigate(R.id.action_homeTabFragment_to_deliveryReqFragment)
        }*/
    }
}