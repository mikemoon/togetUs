package sky.kr.co.newtogetusa.ui.others.terms

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import sky.kr.co.newtogetusa.ui.main.search.SearchResultViewModel.Event
import javax.inject.Inject

@HiltViewModel
class PlayerTermViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory) : BaseViewModel(baseViewModelFactory.create()) {

    val termLevel = MutableStateFlow(0)

    fun onAllAgreeClick(){
        val next = !(allAgree.value ?: false)
        _agree19.value       = next
        _agreeContract.value = next
        _agreeLocation.value = next
        _agreeUnique.value   = next
        _agreeThird.value    = next
    }

    private val _agree19       = MutableStateFlow(false)
    private val _agreeContract = MutableStateFlow(false)
    private val _agreeLocation = MutableStateFlow(false)
    private val _agreeUnique   = MutableStateFlow(false)
    private val _agreeThird    = MutableStateFlow(false)
    val agree19       = _agree19.asStateFlow()
    val agreeContract = _agreeContract.asStateFlow()
    val agreeLocation = _agreeLocation.asStateFlow()
    val agreeUnique   = _agreeUnique.asStateFlow()
    val agreeThird    = _agreeThird.asStateFlow()
    private val allAgreeFlow = combine(
        agree19, agreeContract, agreeLocation, agreeUnique, agreeThird
    ) { arr -> arr.all { it } }
    val allAgree: LiveData<Boolean> = allAgreeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
        .asLiveData()
    fun onAgreeClick(option:Int){
        when(option) {
            0 -> _agree19.value       = !_agree19.value
            1 -> _agreeContract.value = !_agreeContract.value
            2 -> _agreeLocation.value = !_agreeLocation.value
            3 -> _agreeUnique.value   = !_agreeUnique.value
            4 -> _agreeThird.value    = !_agreeThird.value
        }
    }

    fun onNextClick(level:Int){
        setTermLevel(level)
    }

    fun setTermLevel(level: Int){
        termLevel.value = level
    }

    private val _photo = MutableStateFlow<Uri?>(null)
    val photo = _photo.asStateFlow()
    fun setPhoto(uri: Uri){
        _photo.value = uri
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> get() = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }
    sealed class Event{
        object Back :Event()
        object Cancel : Event()
        object GetPhoto : Event()
    }
}