package sky.kr.co.newtogetusa.ui.main.my.profilePage

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
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

    override fun init() {
        super.init()
        reviewAdapter = ReviewAdapter()
        reviewAdapter.setItems(listOf("a", "b", "c"))
        dataBinding.rv.apply {
            adapter = reviewAdapter
            setPadding(paddingLeft, paddingTop, paddingRight, 20.dpToPx())
            clipToPadding = false
        }
    }

    override fun initObserver() {
        super.initObserver()
    }
}