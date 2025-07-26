package sky.kr.co.newtogetusa.ui.main.home.joinPlayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class PlayerJoinViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val step = MutableLiveData<Int>(1)

        val agreeButtonEnable = MutableLiveData<Boolean>()

        val ageOver = MutableLiveData<Boolean>()
    fun onAgeOverClick(isAgree: Boolean){
        ageOver.value = isAgree
    }

        val workAssign = MutableLiveData<Boolean>()
    fun onWorkAssignClick(isAgree: Boolean){
        workAssign.value = isAgree
    }

    val location = MutableLiveData<Boolean>()
    fun onLocationClick(isAgree: Boolean){
        location.value = isAgree
    }

    val uniqueInfo = MutableLiveData<Boolean>()
    fun onUniqueInfoClick(isAgree: Boolean){
        uniqueInfo.value = isAgree
    }

    val personalInfo = MutableLiveData<Boolean>()
    fun onPersonalInfoClick(isAgree: Boolean){
        personalInfo.value = isAgree
    }

    fun onClickStepNext(step: Int){
        this.step.value = step
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
    }
}