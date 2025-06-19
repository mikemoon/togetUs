package sky.kr.co.newtogetusa.ui.others.terms

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPlayerTermBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomPhotoDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class PlayerTermFragment : BaseFragment<FragmentPlayerTermBinding, PlayerTermViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_player_term
    override val viewModel: PlayerTermViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.termLevel.collectLatest {
                    if(it == 4){
                        findNavController().navigate(R.id.playerTermCompleteFragment)
                    }else {
                        setLevelUi(it)
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                PlayerTermViewModel.Event.Back -> {
                    if(viewModel.termLevel.value == 0) {
                        findNavController().popBackStack()
                    }else{
                        viewModel.setTermLevel(viewModel.termLevel.value - 1)
                    }
                }
                PlayerTermViewModel.Event.Cancel -> {
                    findNavController().popBackStack(R.id.searchFragment, false)
                }
                PlayerTermViewModel.Event.GetPhoto -> {
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomPhotoDialog().apply {
                            selectUrl = {
                                this@PlayerTermFragment.viewModel.setPhoto(it)
                            }
                        }
                    )
                }
            }
        }

    }

    private fun setLevelUi(level: Int){
        dataBinding.vLv0.isSelected = level == 0
        dataBinding.vLv1.isSelected = level == 1
        dataBinding.vLv2.isSelected = level == 2
        dataBinding.vLv3.isSelected = level == 3

        dataBinding.layoutLv0.root.isVisible = level == 0
        dataBinding.layoutLv1.root.isVisible = level == 1
        dataBinding.layoutLv2.root.isVisible = level == 2
        dataBinding.layoutLv3.root.isVisible = level == 3
    }

}