package sky.kr.co.newtogetusa.ui.main.my.term

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentTermDetailBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class TermDetailFragment : BaseFragment<FragmentTermDetailBinding, TermDetailViewModel>() {
    override val layoutId: Int = R.layout.fragment_term_detail
    override val viewModel: TermDetailViewModel by viewModels()

    private val args: TermDetailFragmentArgs by navArgs()

    override fun init() {
        super.init()
        dataBinding.tvTitle.text = args.title
        bindContent(args.content)
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                TermDetailViewModel.Event.Back -> findNavController().popBackStack()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun bindContent(content: String) {
        val url = content.trim()
        if (url.startsWith("http://") || url.startsWith("https://")) {
            dataBinding.svContent.visibility = View.GONE
            dataBinding.wvContent.visibility = View.VISIBLE
            dataBinding.wvContent.webViewClient = WebViewClient()
            dataBinding.wvContent.settings.javaScriptEnabled = true
            dataBinding.wvContent.settings.domStorageEnabled = true
            dataBinding.wvContent.loadUrl(url)
        } else {
            dataBinding.wvContent.visibility = View.GONE
            dataBinding.svContent.visibility = View.VISIBLE
            dataBinding.tvContent.text = content
        }
    }
}
