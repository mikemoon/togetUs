package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryProductViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) : BaseViewModel(baseViewModelDependenciesFactory.create()){


    val attachImagesUrl = MutableStateFlow<List<String>>(emptyList())

    val productTitle = MutableStateFlow("")
    val productDescription = MutableStateFlow("")
    val productType = MutableStateFlow("")
    val productWeight = MutableStateFlow("")
    val productVolume= MutableStateFlow("")

    val selectedCompleteButtonEnable =
        combine(
            productTitle,
            productDescription,
            productType,
            productWeight,
            productVolume,
            attachImagesUrl
        ) { values ->
            val title = values[0] as String
            val desc = values[1] as String
            val type = values[2] as String
            val weight = values[3] as String
            val volume = values[4] as String
            val images = values[5] as List<String>

            title.isNotBlank() &&
                    desc.isNotBlank() &&
                    type.isNotBlank() &&
                    weight.isNotBlank() &&
                    volume.isNotBlank() &&
                    images.isNotEmpty()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    fun addAttachImages(paths: List<String>) {
        attachImagesUrl.value = attachImagesUrl.value + paths
    }

    fun removeAttachImage(path: String) {
        attachImagesUrl.value = attachImagesUrl.value - path
    }


    private val _event = SingleLiveEvent<Event>()
    val event: LiveData<Event> = _event
    fun onEventClick(event: Event){
        _event.value = event
    }

    sealed class Event {
        object Back : Event()

        object SelectedComplete : Event()
    }


}