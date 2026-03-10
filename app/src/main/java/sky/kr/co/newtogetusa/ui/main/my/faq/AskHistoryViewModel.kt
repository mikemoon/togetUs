package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.my.InquiryDto
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class AskHistoryViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                              private val myRepository: MyRepository)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){


        val inquiryList = MutableStateFlow<List<InquiryDto>>(listOf())
        fun getOneOnOne() = viewModelScope.launch {
            when(val res = myRepository.putOneOnOne()) {
                is ResultWrapper.Success -> {
                    inquiryList.value = res.data
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
        data class AskItem(val data: InquiryDto) : Event()
    }
}