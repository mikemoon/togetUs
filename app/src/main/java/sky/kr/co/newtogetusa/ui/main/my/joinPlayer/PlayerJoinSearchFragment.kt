package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentPlayerJoinSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartKakaoSearchResultAdapter
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartRecentlyAdapter
import sky.kr.co.newtogetusa.ui.main.search.player.SearchVoiceDialogFragment
import sky.kr.co.newtogetusa.utils.hideKeyboard
import sky.kr.co.newtogetusa.utils.showKeyboard

@AndroidEntryPoint
class PlayerJoinSearchFragment : BaseFragment<FragmentPlayerJoinSearchBinding, PlayerJoinSearchVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_player_join_search
    override val viewModel: PlayerJoinSearchVM by viewModels()

    private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter
    private lateinit var recentlyAdapter: DeliveryStartRecentlyAdapter
    private var hasSearchResults = false

    private val args : PlayerJoinSearchFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.isStartArea.value = args.isStart
        viewModel.isSecondary.value = args.isSecondary

        // 최근 검색 어댑터
        recentlyAdapter = DeliveryStartRecentlyAdapter(
            onClick = { keyword ->
                dataBinding.etSearch.setText(keyword)
                viewModel.onSearchClick()
                hideSearchKeyboard()
            },
            onRemove = { keyword ->
                viewModel.removeKeyword(keyword)
            }
        )
        dataBinding.rvRecently.adapter = recentlyAdapter

        // 검색 결과 어댑터
        searchResultAdapter = DeliveryStartKakaoSearchResultAdapter{ kakaoSearchModel ->
            hideSearchKeyboard()
            dataBinding.tvSearchResult.text = formatAddressDisplay(kakaoSearchModel)
            viewModel.onKakaoAddressClick(kakaoSearchModel)
            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.AREA_SET
            viewModel.setEditMode(false)
        }

        dataBinding.rv.apply {
            adapter = searchResultAdapter
        }

        // IME 검색 액션 처리
        dataBinding.etSearch.setOnEditorActionListener { view, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterUp = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_UP
            if (isSearchAction || isEnterUp) {
                viewModel.onSearchClick()
                hideSearchKeyboard()
                true
            } else {
                false
            }
        }

        // 전체삭제 버튼
        dataBinding.tvDeleteAll.setOnClickListener {
            viewModel.clearRecentSearch()
        }

        // iOS와 동일: 기존 주소가 전달되면 바로 반경설정 모드로 진입 (검색 화면 건너뜀)
        val existingAddress = arguments?.getParcelable<KakaoSearchModel>("existingAddress")
        val existingRadius = arguments?.getInt("existingRadius", 3) ?: 3
        if (existingAddress != null && existingAddress.lat != null && existingAddress.lat > 0
            && existingAddress.lng != null && existingAddress.lng > 0) {
            // 기존 주소가 있으면 바로 반경설정 모드 (iOS ChooseRouteViewController)
            dataBinding.tvSearchResult.text = formatAddressDisplay(existingAddress)
            viewModel.onKakaoAddressClick(existingAddress)
            val clampedRadius = existingRadius.coerceIn(1, 5)
            viewModel.setAreaRadius(clampedRadius.toFloat())
            dataBinding.slide.value = clampedRadius.toFloat()
            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.AREA_SET
            viewModel.setEditMode(false)
        } else {
            // 기존 주소가 없으면 검색 모드 (iOS SearchAddressViewController)
            // 포커스/키보드는 isEditMode 수집부에서 처리
        }
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.slide.addOnChangeListener { _, value, _ ->
            viewModel.setAreaRadius(value)
        }

        // 최근 검색어 목록
        viewModel.recentSearchList.observe(viewLifecycleOwner) { list ->
            recentlyAdapter.submitList(list)
            updateRecentSearchVisibility()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.searchStep.collectLatest { step ->
                        when (step) {
                            PlayerJoinSearchVM.SearchStep.RECENT -> {
                                dataBinding.llRecent.isVisible = true
                                dataBinding.llSearch.isVisible = false
                                dataBinding.clArea2.isVisible = false
                            }

                            PlayerJoinSearchVM.SearchStep.SEARCH -> {
                                dataBinding.llRecent.isVisible = false
                                dataBinding.llSearch.isVisible = true
                                dataBinding.clArea2.isVisible = false
                            }

                            PlayerJoinSearchVM.SearchStep.AREA_SET -> {
                                dataBinding.llRecent.isVisible = false
                                dataBinding.llSearch.isVisible = false
                                dataBinding.clArea2.isVisible = true
                            }
                        }
                    }
                }

                launch {
                    searchResultAdapter.loadStateFlow.collectLatest { loadStates ->
                        val isRefreshNotLoading = loadStates.refresh is LoadState.NotLoading
                        val isEmpty = isRefreshNotLoading && searchResultAdapter.itemCount == 0
                        hasSearchResults = isRefreshNotLoading && searchResultAdapter.itemCount > 0

                        if (viewModel.searchStep.value == PlayerJoinSearchVM.SearchStep.SEARCH) {
                            if (isEmpty) {
                                viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.RECENT
                            }
                        }
                        updateRecentSearchVisibility()
                    }
                }


                launch {
                    viewModel.results.collectLatest { pagingData ->
                        searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                    }
                }

                launch {
                    viewModel.isEditMode.collectLatest { isEditMode ->
                        setEditMode(isEditMode)
                        // 검색모드 진입(신규 진입 또는 '수정' 탭) 시 포커스 + 키보드 표시
                        if (isEditMode) {
                            showSearchKeyboard()
                        }
                    }
                }

                launch {
                    viewModel.query.collectLatest { query ->
                        val current = dataBinding.etSearch.text?.toString().orEmpty()
                        if (current != query) {
                            dataBinding.etSearch.setText(query)
                            dataBinding.etSearch.setSelection(query.length)
                        }
                        dataBinding.ivDelete.isVisible = query.isNotBlank()
                    }
                }

                launch {
                    viewModel.areaRadius.collectLatest { km ->
                        dataBinding.tvAreaDistance.text = "${km}km"
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                PlayerJoinSearchVM.Event.Back ->{
                    findNavController().popBackStack()
                }
                PlayerJoinSearchVM.Event.SearchFromMap ->{
                    // 검색에서 선택된 주소가 있으면 지도 초기 위치로 전달
                    val bundle = Bundle().apply {
                        viewModel.currentSelectedAddress?.let {
                            val lat = it.lat
                            val lng = it.lng
                            if (lat != null && lng != null && lat > 0 && lng > 0) {
                                putParcelable("initialAddress", it)
                            }
                        }
                    }
                    findNavController().navigate(R.id.deliveryAddressMapFragment, bundle)
                }
                PlayerJoinSearchVM.Event.VoiceSearch -> {
                    SearchVoiceDialogFragment()
                        .onRecognized { recognizedText ->
                            viewModel.setVoiceSearchText(recognizedText)
                        }
                        .show(childFragmentManager, "SearchVoiceDialogFragment")
                }
                PlayerJoinSearchVM.Event.SelectedComplete ->{
                    val selected = viewModel.currentSelectedAddress ?: return@observe

                    val bundle = Bundle().apply {
                        putParcelable("selectedKakaoLocValue", selected)
                        putBoolean("isStart", viewModel.isStartArea.value)
                        putInt("areaRadius", dataBinding.slide.value.toInt())
                        putBoolean("isSecondary", viewModel.isSecondary.value)
                    }

                    // Activity의 FragmentManager에 결과를 전달 (모든 Fragment에서 수신 가능)
                    requireActivity().supportFragmentManager.setFragmentResult("fromPlayerJoinSearch", bundle)

                    findNavController().popBackStack()
                }
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            "fromC",
            viewLifecycleOwner
        ) { _, bundle ->

            val result =
                bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                    ?: return@setFragmentResultListener

            hideSearchKeyboard()

            // UI 반영
            dataBinding.tvSearchResult.text = formatAddressDisplay(result)

            // ViewModel 반영
            viewModel.onKakaoAddressClick(result)

            // 검색 결과 영역 숨기고 반경 설정으로 전환
            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.AREA_SET
            viewModel.setEditMode(false)
        }
    }

    private fun setEditMode(isEditMode : Boolean){
        dataBinding.clSearch.isVisible = isEditMode
        dataBinding.clSelectedAddress.isVisible = !isEditMode

        // iOS와 동일하게 divider 위치를 모드에 따라 변경
        // 검색모드: clSearch 아래 / 반경설정모드: clSelectedAddress 아래
        val dividerParams = dataBinding.divider.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        if (isEditMode) {
            dividerParams.topToBottom = dataBinding.clSearch.id
        } else {
            dividerParams.topToBottom = dataBinding.clSelectedAddress.id
        }
        dataBinding.divider.layoutParams = dividerParams
    }

    private fun updateRecentSearchVisibility() {
        if (viewModel.searchStep.value == PlayerJoinSearchVM.SearchStep.RECENT) {
            val hasRecent = (viewModel.recentSearchList.value?.size ?: 0) > 0
            dataBinding.llRecent.isVisible = true
        }
    }

    /**
     * iOS와 동일한 주소 표시 형식:
     * - placeName(subtitle)이 있으면: "addressName (placeName)"
     * - placeName이 없으면: "addressName"
     */
    private fun formatAddressDisplay(model: KakaoSearchModel): String {
        val placeName = model.subtitle
        return if (!placeName.isNullOrBlank()) {
            "${model.name} ($placeName)"
        } else {
            model.name
        }
    }

    private fun showSearchKeyboard() {
        dataBinding.etSearch.post {
            dataBinding.etSearch.requestFocus()
            val controller = androidx.core.view.ViewCompat
                .getWindowInsetsController(dataBinding.etSearch)
            if (controller != null) {
                controller.show(androidx.core.view.WindowInsetsCompat.Type.ime())
            } else {
                requireContext().showKeyboard(dataBinding.etSearch)
            }
        }
    }

    private fun hideSearchKeyboard() {
        dataBinding.etSearch.clearFocus()
        requireContext().hideKeyboard(dataBinding.etSearch)
    }

}