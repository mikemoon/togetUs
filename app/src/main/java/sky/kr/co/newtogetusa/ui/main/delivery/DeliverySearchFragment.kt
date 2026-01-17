package sky.kr.co.newtogetusa.ui.main.delivery

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliverySearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.showKeyboard
import timber.log.Timber

@AndroidEntryPoint
class DeliverySearchFragment :
    BaseFragment<FragmentDeliverySearchBinding, DeliverySearchViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_search
    override val viewModel: DeliverySearchViewModel by viewModels()

    private lateinit var recentlyAdapter : DeliveryStartRecentlyAdapter
    private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter

    private val args: DeliverySearchFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.isStart.value = args.isStart
        viewModel.isInternational.value = args.isInternational

        recentlyAdapter = DeliveryStartRecentlyAdapter(
            onClick = { keyword ->
                dataBinding.etAddress.setText(keyword)
                viewModel.onSearchClick()
            },
            onRemove = { keyword ->
                viewModel.removeKeyword(keyword)
            }
        )
        dataBinding.rvRecently.adapter = recentlyAdapter

        searchResultAdapter = DeliveryStartKakaoSearchResultAdapter(viewModel)
        dataBinding.rvSearchResult.adapter = searchResultAdapter

        dataBinding.etAddress.requestFocus()
        requireContext().showKeyboard(dataBinding.etAddress)

    }

    override fun initObserver() {
        super.initObserver()

        viewModel.recentSearchList.observe(viewLifecycleOwner) { list ->
            recentlyAdapter.submitList(list)
            dataBinding.rvRecently.visibility =
                if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        lifecycleScope.launch {
            viewModel.results.collectLatest { pagingData ->
                // Paging 데이터 붙이기
                searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            }
        }

        // 로딩/에러/빈 상태 처리(선택)
        lifecycleScope.launch {
            searchResultAdapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                val isError = loadStates.refresh is LoadState.Error
                val isEmpty = loadStates.refresh is LoadState.NotLoading &&
                        searchResultAdapter.itemCount == 0

                //dataBinding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
                dataBinding.rvSearchResult.visibility =
                    if (!isLoading && !isEmpty) View.VISIBLE else View.GONE
                //dataBinding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE

                if (isError) {
                    val e = (loadStates.refresh as LoadState.Error).error
                    Timber.e(e, "주소 검색 실패")
                }
            }
        }

        viewModel.selectedAddress.observe(viewLifecycleOwner) {
            val bundle = Bundle().apply {
                putBoolean("isStart", true)
                putParcelable("selectedKakaoLocValue", it)
            }
            // 결과 전달
            parentFragmentManager.setFragmentResult("fromC", bundle)
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) { ev ->
            when (ev) {
                is DeliverySearchViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }
            }
        }
    }
}