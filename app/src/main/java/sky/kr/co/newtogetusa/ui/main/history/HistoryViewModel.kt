package sky.kr.co.newtogetusa.ui.main.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliveryDetailResponse
import sky.kr.co.newtogetusa.data.remote.dto.delivery.DeliverySummaryDto
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                           private val deliveryRepo : DeliveryRepository) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val menuAll = TopMenu.All
    val menuDoing = TopMenu.Doing
    val menuEnd = TopMenu.End

    val deliveryDetail = MutableStateFlow<DeliveryDetailResponse?>(null)

    val isModePlayer = MutableStateFlow<Boolean?>(null)
    init {
        viewModelScope.launch {
            dataStoreRepository.getBooleanFlow(DataStoreKey.KEY_IS_MODE_PLAYER).filterNotNull()
                .collectLatest { isPlayerMode ->
                    isModePlayer.value = isPlayerMode
                }
        }
    }

    private val _topMenu = MutableStateFlow("ALL")
    private val _selectedTopMenu = MutableStateFlow<TopMenu>(TopMenu.All)
    val selectedTopMenu: StateFlow<TopMenu> = _selectedTopMenu
    private val _keyword = MutableStateFlow("")

    fun setKeyword(keyword: String){
        _keyword.value = keyword
    }
    fun onTopMenuSelect(topMenu: TopMenu) {
        _topMenu.value = when(topMenu){
            is TopMenu.All -> "ALL"
            is TopMenu.Doing -> "ING"//"REG|MATCH|DELIVERY|ING"
            is TopMenu.End -> "DONE"
        }
        _selectedTopMenu.value = topMenu
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deliveryPagingFlow =
        combine(_topMenu, _keyword) { type, keyword ->
            type to keyword
        }.flatMapLatest { (type, keyword) ->
            deliveryRepo.getDeliveryPagingFlow(type, keyword)
        }.cachedIn(viewModelScope)

    val allDeliveryList = MutableStateFlow<List<DeliverySummaryDto>?>(null)
    fun postDeliverySearch(deliverySearchReq: DeliverySearchReq) = viewModelScope.launch {
        val res = deliveryRepo.postDeliverySearch(deliverySearchReq)
        when(res){
            is ResultWrapper.Success ->{
                allDeliveryList.value = res.data.deliveries
            }
            else -> {}
        }
    }

    private val _historyFlow = MutableStateFlow<PagingData<String>>(PagingData.empty())
    val historyFlow : StateFlow<PagingData<String>> = _historyFlow

    private val _itemCancelLiveData = SingleLiveEvent<DeliverySummaryDto>()
    val itemCancelLiveData: LiveData<DeliverySummaryDto> = _itemCancelLiveData
    fun onItemCancel(item:DeliverySummaryDto){
        _itemCancelLiveData.value = item
    }

    private val _menuButtonLiveData = SingleLiveEvent<MenuButton>()
    val menuButtonLiveData: LiveData<MenuButton> = _menuButtonLiveData
    fun onMenuBottonClick(menuAction: MenuButton) {
        _menuButtonLiveData.value = menuAction
    }

    fun confirmRequesterPickup(item: DeliverySummaryDto) = viewModelScope.launch {
        when (deliveryRepo.putRequesterPickup(item.delivery_id)) {
            is ResultWrapper.Success -> _menuButtonLiveData.value = MenuButton.MenuRefresh("수령 확인이 완료되었어요.")
            else -> _menuButtonLiveData.value = MenuButton.MenuError("수령 확인에 실패했습니다.")
        }
    }

    sealed class TopMenu {
        object All : TopMenu()
        object Doing : TopMenu()
        object End : TopMenu()

    }

    sealed class MenuButton{
        data class MenuModify(val item: DeliverySummaryDto) : MenuButton()
        data class MenuDeliveryStatus(val item: DeliverySummaryDto) : MenuButton()
        data class MenuChat(val item: DeliverySummaryDto) : MenuButton()
        data class MenuReview(val item: DeliverySummaryDto) : MenuButton()
        data class MenuRefresh(val message: String) : MenuButton()
        data class MenuError(val message: String) : MenuButton()
    }
}
