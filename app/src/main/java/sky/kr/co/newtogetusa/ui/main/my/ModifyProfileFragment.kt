package sky.kr.co.newtogetusa.ui.main.my

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentModifyProfileBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class ModifyProfileFragment : BaseFragment<FragmentModifyProfileBinding, ModifyProfileViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_modify_profile
    override val viewModel: ModifyProfileViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is ModifyProfileViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}