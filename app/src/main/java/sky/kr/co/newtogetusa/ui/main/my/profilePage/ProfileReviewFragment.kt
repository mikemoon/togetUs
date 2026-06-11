package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.data.remote.dto.users.PlayerInfoDto
import sky.kr.co.newtogetusa.databinding.FragmentProfileReviewBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.my.ProfileManagementViewModel
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class ProfileReviewFragment : BaseFragment<FragmentProfileReviewBinding, ProfileManagementViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_profile_review
    override val viewModel: ProfileManagementViewModel by viewModels({requireParentFragment()})

    private lateinit var reviewAdapter : ReviewAdapter
    private var reviews: List<PlayerInfoDto> = emptyList()
    private var reviewCodes: List<BaseCommonDto> = emptyList()

    override fun init() {
        super.init()
        val isFromSearchResult = arguments?.getBoolean(ARG_FROM_SEARCH_RESULT, false) ?: false
        reviewAdapter = ReviewAdapter(hideAddress = isFromSearchResult)
        dataBinding.rv.apply {
            adapter = reviewAdapter
            setPadding(paddingLeft, paddingTop, paddingRight, 20.dpToPx())
            clipToPadding = false
        }

        viewModel.getPlayerReviewCodes {
            reviewCodes = it
            updateReviews()
        }
        viewModel.getPlayerReviews {
            reviews = it
            updateReviews()
        }
    }

    override fun initObserver() {
        super.initObserver()
    }

    private fun updateReviews() {
        reviewAdapter.setItems(reviews, reviewCodes)
    }

    companion object {
        const val ARG_FROM_SEARCH_RESULT = "ARG_FROM_SEARCH_RESULT"
    }
}
