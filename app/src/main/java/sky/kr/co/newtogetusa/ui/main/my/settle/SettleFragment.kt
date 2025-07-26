package sky.kr.co.newtogetusa.ui.main.my.settle

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentSettleBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomDateDialog
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow

@AndroidEntryPoint
class SettleFragment : BaseFragment<FragmentSettleBinding, SettleViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_settle
    override val viewModel: SettleViewModel by viewModels()

    private lateinit var settleAdapter: SettleAdapter

    override fun init() {
        super.init()
        settleAdapter = SettleAdapter(viewModel).apply {
            setItems(listOf(1,2,3))
        }
        dataBinding.rv.apply {
            adapter = settleAdapter
        }
    }
    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                SettleViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
                SettleViewModel.Event.SettleGuide ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        MessageDialog.newInstance(
                            msgTitle = "정산 안내",
                            msg = "정산 예정일?\n" +
                                    "배송완료일 기준, 차차주 화요일에 회원님 명의의 계좌로 자동 입금처리 됩니다.\n" +
                                    "(예: 5/25(일) ~ 5/31(토) 한주간 배송완료된 건에 대한 정산금을 그 다다음주 화요일(6/10)에 회원님의 계좌로 입금)\n" +
                                    "\n" +
                                    "정산보류?\n" +
                                    "정산금액 지급 시 회원님 명의의 계좌가 아니거나 배송건 분쟁 등으로 입금이 지연되는 경우에 해당합니다. 고객센터로 문의해주세요.\n" +
                                    "\n" +
                                    "원천징수 차감액?\n" +
                                    "회원님의 누적 정산금액이 30만원을 초과하면, ‘기타소득’으로 분류되어 정산금액의 22%(소득세 20% + 지방소득세 2%)를 원천징수 후 정산해 드려요.\n" +
                                    "원천징수영수증은 년말에 요청하실 수 있어요.",
                            rightBtn = "확인"
                        )
                    )
                }
                SettleViewModel.Event.AccountInfo ->{
                    findNavController().navigate(R.id.action_settleFragment_to_accountInfoFragment)
                }
                SettleViewModel.Event.Date ->{
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomDateDialog()
                    )
                }
            }
        }
    }
}