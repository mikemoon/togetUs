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
            imageBytes: ByteArray? = null,
            resultCallback:(Boolean) -> Unit
        ) = viewModelScope.launch {
            // iOS 대응: multipart 전송 (base64 대비 전송량 약 33% 감소)
            when(val res = myRepository.postOneOnOne(
                inquiry = content,
                phone = phone,
                imageBytesList = listOfNotNull(imageBytes)
            )){
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