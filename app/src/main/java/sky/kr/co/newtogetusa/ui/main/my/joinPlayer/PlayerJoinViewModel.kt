package sky.kr.co.newtogetusa.ui.main.my.joinPlayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class PlayerJoinViewModel @Inject constructor(
    baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val playerRepository: PlayerRepository
) : BaseViewModel(baseViewModelDependenciesFactory.create()) {

    val step = MutableLiveData<Int>(1)

    val enableStep1Next = MutableLiveData<Boolean>()

    val allAgreeStep1 = MutableLiveData<Boolean>(false)

    fun onAllAgreeClick(isAgree: Boolean) {
        allAgreeStep1.value = isAgree
        ageOver.value = isAgree
        workAssign.value = isAgree
        location.value = isAgree
        uniqueInfo.value = isAgree
        personalInfo.value = isAgree
        updateStep1NextButtonState()
    }

    fun postPlayer() = viewModelScope.launch {
        playerRepository.postPlayer(hashMapOf())
    }

    val ageOver = MutableLiveData<Boolean>(false)
    fun onAgeOverClick(isAgree: Boolean) {
        ageOver.value = isAgree
        updateStep1NextButtonState()
    }

    val workAssign = MutableLiveData<Boolean>(false)
    fun onWorkAssignClick(isAgree: Boolean) {
        workAssign.value = isAgree
        updateStep1NextButtonState()
    }

    val location = MutableLiveData<Boolean>(false)
    fun onLocationClick(isAgree: Boolean) {
        location.value = isAgree
        updateStep1NextButtonState()
    }

    val uniqueInfo = MutableLiveData<Boolean>(false)
    fun onUniqueInfoClick(isAgree: Boolean) {
        uniqueInfo.value = isAgree
        updateStep1NextButtonState()
    }

    val personalInfo = MutableLiveData<Boolean>(false)
    fun onPersonalInfoClick(isAgree: Boolean) {
        personalInfo.value = isAgree
        updateStep1NextButtonState()
    }

    fun onClickStepNext(step: Int) {
        this.step.value = step
    }

    private fun updateStep1NextButtonState() {
        enableStep1Next.value =
            ageOver.value == true && workAssign.value == true && location.value == true && uniqueInfo.value == true && personalInfo.value == true
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object AttachImage : Event()
        object AccountNumber : Event()
        object Bank : Event()
        object Complete : Event()
        object StartArea : Event()
        object DestinaitonArea : Event()
    }
}