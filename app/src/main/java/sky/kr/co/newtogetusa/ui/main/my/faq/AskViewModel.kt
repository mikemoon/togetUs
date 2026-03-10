package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class AskViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                       private val myRepository: MyRepository)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        fun postOneOnOne(
            content:String,
            phone:String,
            imageBase64: String? = null,
            resultCallback:(Boolean) -> Unit
        ) = viewModelScope.launch {
            when(val res = myRepository.postOneOnOne(hashMapOf(
                "inquiry" to content,
                "phone" to phone,
            ).apply {
                if(!imageBase64.isNullOrEmpty()) {
                    put("attach_base64", imageBase64)
                    put("attach_mime", "image/png")
                }
            })){
                is ResultWrapper.Success -> {
                    resultCallback(res.data)
                }
                else -> {}
            }
        }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        object ShowPrivacy : Event()
        object AttachImage : Event()
    }
}