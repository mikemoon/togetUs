package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPlayerDeliveryNoticeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class PlayerDeliveryNoticeFragment :
    BaseFragment<FragmentPlayerDeliveryNoticeBinding, PlayerDeliveryNoticeVM>() {

    override val layoutId: Int = R.layout.fragment_player_delivery_notice
    override val viewModel: PlayerDeliveryNoticeVM by viewModels()

    override fun init() {
        super.init()

        dataBinding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}