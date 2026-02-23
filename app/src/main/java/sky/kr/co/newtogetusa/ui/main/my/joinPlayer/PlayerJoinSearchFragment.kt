package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentPlayerJoinSearchBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.delivery.DeliveryStartKakaoSearchResultAdapter

@AndroidEntryPoint
class PlayerJoinSearchFragment : BaseFragment<FragmentPlayerJoinSearchBinding, PlayerJoinSearchVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_player_join_search
    override val viewModel: PlayerJoinSearchVM by viewModels()

    private lateinit var searchResultAdapter: DeliveryStartKakaoSearchResultAdapter

    private val args : PlayerJoinSearchFragmentArgs by navArgs()

    override fun init() {
        super.init()

        viewModel.isStartArea.value = args.isStart
        viewModel.isSecondary.value = args.isSecondary

        searchResultAdapter = DeliveryStartKakaoSearchResultAdapter{ kakaoSearchModel ->
            dataBinding.tvSearchResult.text = kakaoSearchModel.name
            viewModel.onKakaoAddressClick(kakaoSearchModel)
            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.AREA_SET
            viewModel.setEditMode(false)
        }

        dataBinding.rv.apply {
            adapter = searchResultAdapter
        }
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.slide.addOnChangeListener { _, value, _ ->
            viewModel.setAreaRadius(value)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.searchStep.collectLatest { step ->
                        when (step) {
                            PlayerJoinSearchVM.SearchStep.NONE -> {
                                dataBinding.llSearch.isVisible = false
                                dataBinding.clArea2.isVisible = false
                            }

                            PlayerJoinSearchVM.SearchStep.SEARCH -> {
                                dataBinding.llSearch.isVisible = true
                                dataBinding.clArea2.isVisible = false
                            }

                            PlayerJoinSearchVM.SearchStep.AREA_SET -> {
                                dataBinding.clArea2.isVisible = true
                                dataBinding.llSearch.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    searchResultAdapter.loadStateFlow.collectLatest { loadStates ->

                        val isEmpty =
                            loadStates.refresh is androidx.paging.LoadState.NotLoading &&
                                    searchResultAdapter.itemCount == 0

                        /*if (viewModel.query.value.length < 2) {
                            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.NONE
                            return@collectLatest
                        }*/

                        if (isEmpty) {
                            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.NONE
                        } else {
                            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.SEARCH
                        }
                    }
                }


                launch {
                    viewModel.results.collectLatest { pagingData ->
                        // Paging 데이터 붙이기
                        searchResultAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                    }
                }

                launch {
                    viewModel.isEditMode.collectLatest { isEditMode ->
                        setEditMode(isEditMode)
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
                    findNavController().navigate(R.id.deliveryAddressMapFragment)
                }
                PlayerJoinSearchVM.Event.SelectedComplete ->{
                    val selected = viewModel.selectedAddress.value ?: return@observe

                    val bundle = Bundle().apply {
                        putParcelable("selectedKakaoLocValue", selected)
                        putBoolean("isStart", viewModel.isStartArea.value)
                        putInt("areaRadius", dataBinding.slide.value.toInt())
                        putBoolean("isSecondary", viewModel.isSecondary.value)
                        // ViewModel에 현재 시작/도착 구분값 가지고 있어야 함
                    }

                    parentFragmentManager.setFragmentResult("fromPlayerJoinSearch", bundle)
                    findNavController().popBackStack()
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            "fromC",
            viewLifecycleOwner
        ) { _, bundle ->

            val result =
                bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue")
                    ?: return@setFragmentResultListener

            // UI 반영
            dataBinding.tvSearchResult.text = result.name

            // ViewModel 반영
            viewModel.onKakaoAddressClick(result)

            // 검색 결과 영역 숨기고 반경 설정으로 전환
            viewModel.searchStep.value = PlayerJoinSearchVM.SearchStep.AREA_SET
            viewModel.setEditMode(false)
        }
    }

    private fun setEditMode(isEditMode : Boolean){
        dataBinding.clEdit.isVisible = isEditMode
        dataBinding.tvSearchResult.isVisible = !isEditMode
        dataBinding.tvModify.isVisible = !isEditMode
    }

}