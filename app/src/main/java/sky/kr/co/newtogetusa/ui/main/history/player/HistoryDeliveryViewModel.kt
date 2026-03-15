package sky.kr.co.newtogetusa.ui.main.history.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.history.HistoryViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryDeliveryViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                                   private val deliveryRepo : DeliveryRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){


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
    private val _keyword = MutableStateFlow("")

    private val _itemCancelLiveData = SingleLiveEvent<String>()
    val itemCancelLiveData: LiveData<String> = _itemCancelLiveData
    fun onItemCancel(item:String){
        _itemCancelLiveData.value = item
    }

    val menuAll = TopMenu.All
    val menuDoing = TopMenu.Doing
    val menuEnd = TopMenu.End

    private val _topMenuLiveData = SingleLiveEvent<TopMenu>()
    val topMenuLiveData: LiveData<TopMenu> = _topMenuLiveData

    fun onTopMenuSelect(topMenu: TopMenu) {
        _topMenu.value = when(topMenu){
            is TopMenu.All -> "ALL"
            is TopMenu.Doing -> "ING"//"REG|MATCH|DELIVERY|ING"
            is TopMenu.End -> "DONE"
        }
        _topMenuLiveData.value = topMenu
    }

    val isShowCalendar = MutableStateFlow(false)
    fun onShowCalendar(isShow: Boolean){
        isShowCalendar.value = isShow
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val deliveryPagingFlow =
        combine(_topMenu, _keyword) { type, keyword ->
            type to keyword
        }.flatMapLatest { (type, keyword) ->
            deliveryRepo.getPlayerDeliveryPagingFlow(type, keyword)
        }.cachedIn(viewModelScope)

    sealed class TopMenu {
        object All : TopMenu()
        object Doing : TopMenu()
        object End : TopMenu()
    }
}