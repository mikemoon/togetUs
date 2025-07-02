package sky.kr.co.newtogetusa.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginNicknameViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private val _nickname = MutableLiveData<String>("")
    val nickname: LiveData<String> = _nickname
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        Timber.d("onTextChanged $s")
        _nickname.value = s.toString()
        _showClearIcon.value = s.isNotEmpty()
    }

    private val _showClearIcon = MutableLiveData<Boolean>(false)
    val showClearIcon: LiveData<Boolean> = _showClearIcon

    fun onClearClick() {
        _nickname.value = ""
        _showClearIcon.value = false
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object Confirm : Event()
    }
}