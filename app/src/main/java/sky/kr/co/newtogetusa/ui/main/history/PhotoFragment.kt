package sky.kr.co.newtogetusa.ui.main.history

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentPhotoBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class PhotoFragment : BaseFragment<FragmentPhotoBinding, PhotoVM>() {
    override val layoutId: Int = R.layout.fragment_photo
    override val viewModel: PhotoVM by viewModels()

    private lateinit var imageAdapter: PhotoPagerAdapter

    val args : PhotoFragmentArgs by navArgs()

    override fun init() {
        super.init()

        setupViewPager(args.images.toList())
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                PhotoVM.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupViewPager(items: List<String>){
        imageAdapter = PhotoPagerAdapter(items)
        dataBinding.vpImages.apply {
            adapter = imageAdapter
        }
        dataBinding.dotsIndicator.attachTo(dataBinding.vpImages)
    }
}