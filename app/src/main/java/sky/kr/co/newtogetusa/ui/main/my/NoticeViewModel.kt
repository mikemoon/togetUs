package sky.kr.co.newtogetusa.ui.main.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.my.NoticeDto
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                          private val myRepository: MyRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){


        val noticeList = MutableStateFlow<List<NoticeDto>>(listOf())

    fun getNoticeList() = viewModelScope.launch {
        when(val res = myRepository.putNoticeList(hashMapOf("device_type" to "A"))){
            is ResultWrapper.Success ->{
                noticeList.value = res.data
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
        data class NoticeDetail(val id: Int) : Event()
    }
}