package sky.kr.co.newtogetusa.ui.main.history.review

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentReceivedReviewBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.loadProfile
import sky.kr.co.newtogetusa.utils.toast

@AndroidEntryPoint
class ReceivedReviewFragment : BaseFragment<FragmentReceivedReviewBinding, DeliveryReviewViewModel>() {
    override val layoutId: Int = R.layout.fragment_received_review
    override val viewModel: DeliveryReviewViewModel by viewModels()
    private val args: ReceivedReviewFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        viewModel.loadReceivedReview(args.deliveryId, args.isPlayer)
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.receivedReview.collect { review ->
                    if (review == null) return@collect
                    dataBinding.ivProfile.loadProfile(review.profileImage)
                    dataBinding.tvName.text = review.nickname.orEmpty()
                    dataBinding.tvDate.text = review.regDate.orEmpty()
                    dataBinding.tvStars.text = "★".repeat(review.stars.coerceIn(0, 5))
                    dataBinding.tvContents.text = review.contents.orEmpty()
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryReviewViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryReviewViewModel.Event.LoadFailed -> requireContext().toast("받은 후기를 불러오지 못했습니다.")
                else -> Unit
            }
        }
    }
}
