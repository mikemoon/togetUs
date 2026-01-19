package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.BaseCommonDto
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryProductViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory,
    private val configRepository: ConfigRepository
    ) : BaseViewModel(baseViewModelDependenciesFactory.create()){

    val productTypes = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    val productWeights = MutableStateFlow<List<BaseCommonDto>>(emptyList())
    val productVolumes = MutableStateFlow<List<BaseCommonDto>>(emptyList())

        init {
            getConfigProductType()
            getConfigProductWeight()
            getConfigProductVolume()
        }


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


    fun getConfigProductType() = viewModelScope.launch {
        val res = configRepository.getProductTypeList()
        when(res){
            is ResultWrapper.Success -> {
                productTypes.value = res.data
            }
            else -> {}
        }
    }

    fun getConfigProductWeight() = viewModelScope.launch {
        val res = configRepository.getProductWeightList()
        when(res){
            is ResultWrapper.Success -> {
                productWeights.value = res.data
            }
            else -> {}
        }
    }

    fun getConfigProductVolume() = viewModelScope.launch {
        val res = configRepository.getProductVolumeList()
        when(res){
            is ResultWrapper.Success -> {
                productVolumes.value = res.data
            }
            else -> {}
        }
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