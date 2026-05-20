package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class WriteIntroductionViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository,
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val text = MutableStateFlow("")
    val countText = MutableStateFlow("0/500")
    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event

    fun setInitial(value: String) {
        if (text.value.isBlank()) updateText(value)
    }

    fun updateText(value: CharSequence) {
        val trimmed = value.toString().take(500)
        text.value = trimmed
        countText.value = "${trimmed.length}/500"
    }

    fun save(playerId: Int) = viewModelScope.launch {
        if (playerId <= 0) {
            _event.value = Event.SaveFailed
            return@launch
        }
        when (playerRepository.postIntroduction(playerId, hashMapOf("introduction" to text.value))) {
            is ResultWrapper.Success -> _event.value = Event.SaveSuccess
            else -> _event.value = Event.SaveFailed
        }
    }

    fun onBackClick() {
        _event.value = Event.Back
    }

    sealed class Event {
        object Back : Event()
        object SaveSuccess : Event()
        object SaveFailed : Event()
    }
}
