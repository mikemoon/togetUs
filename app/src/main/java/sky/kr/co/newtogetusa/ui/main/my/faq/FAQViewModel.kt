package sky.kr.co.newtogetusa.ui.main.my.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQCateDto
import sky.kr.co.newtogetusa.data.remote.dto.my.FAQDto
import sky.kr.co.newtogetusa.repository.MyRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
                                       private val myRepository: MyRepository)
    :BaseViewModel(baseViewModelDependenciesFactory.create()){

        val faqCateList = MutableStateFlow<List<FAQCateDto>>(listOf())

    fun getFaqCateList() = viewModelScope.launch {
        when(val res = myRepository.putFaqCateList()){
            is ResultWrapper.Success ->{
                faqCateList.value = res.data
            }
            else -> {}
        }
    }

    val faqList = MutableStateFlow<List<FAQDto>>(listOf())
    fun getFaqList(cateId: Int) = viewModelScope.launch {
        faqList.value = getFaqListByCategory(cateId)
    }

    suspend fun getFaqListByCategory(cateId: Int): List<FAQDto> {
        return when(val res = myRepository.putFaqList(hashMapOf("cate_id" to cateId.toString()))){
            is ResultWrapper.Success -> res.data
            else -> emptyList()
        }
    }

    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event) {
        _event.value = event
    }

    sealed class Event {
        object Back : Event()
        data class FAQDetail(val id:Int) : Event()
        object Ask : Event()
        object AskHistory : Event()
    }
}