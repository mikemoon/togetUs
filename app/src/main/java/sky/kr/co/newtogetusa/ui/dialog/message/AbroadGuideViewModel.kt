package sky.kr.co.newtogetusa.ui.dialog.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class AbroadGuideViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

    val isAgreeChecked = MutableStateFlow(false)

    fun onAgreeCheck(isChecked:Boolean){
        isAgreeChecked.value = isChecked
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        when (event) {
            is Event.Agree -> {
                viewModelScope.launch {
                    dataStoreRepository.putBoolean(DataStoreKey.KEY_ABROAD_DELIVERY_AGREE, true)
                    _event.value = event
                }
            }
            else ->{
                _event.value = event
            }
        }
    }

    sealed class Event {
        object Agree : Event()
        object Cancel : Event()
    }
}