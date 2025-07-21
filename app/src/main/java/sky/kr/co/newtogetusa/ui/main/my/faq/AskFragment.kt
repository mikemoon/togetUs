package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentAskBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class AskFragment : BaseFragment<FragmentAskBinding, AskViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_ask
    override val viewModel: AskViewModel by viewModels()

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is AskViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }


    private fun showPrivacyDialog(){
        dialogFragmentShow(
            childFragmentManager,
            MessageDialog.newInstance(
                msgTitle = "개인정보 수집 및 이용",
                msg = "수집하는 개인 정보[(필수) 문의 내용, (선택) 휴대폰 번호, 첨부 파일]는 문의 내용 처리 및 고객 불만을 해결하기 위해 사용되며, 관련 법령에 따라 3년간 보관 후 삭제됩니다.\n" +
                        "\n" +
                        "문의 접수, 처리 및 회신을 위해 꼭 필요한 정보이기 때문에, 동의해 주셔야 서비스를 이용하실 수 있습니다.",
                rightBtn = "확인"
            )
        )
    }
}