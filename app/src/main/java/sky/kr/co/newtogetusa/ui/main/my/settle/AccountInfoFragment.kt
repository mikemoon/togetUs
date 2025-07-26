package sky.kr.co.newtogetusa.ui.main.my.settle

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAccountInfoBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomAccountInfoDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomBankDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class AccountInfoFragment : BaseFragment<FragmentAccountInfoBinding, AccountInfoViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_account_info
    override val viewModel: AccountInfoViewModel by viewModels()

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                AccountInfoViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                AccountInfoViewModel.Event.Bank ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomBankDialog()
                    )
                }
                AccountInfoViewModel.Event.AccountNumber ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomAccountInfoDialog()
                    )
                }
            }
        }
    }
}