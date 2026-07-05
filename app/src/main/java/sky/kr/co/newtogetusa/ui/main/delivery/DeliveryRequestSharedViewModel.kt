package sky.kr.co.newtogetusa.ui.main.delivery

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DeliveryRequestSharedViewModel @Inject constructor() : ViewModel() {

    val attachImagesUrl = MutableStateFlow<List<Uri>>(emptyList())
    private val _state = MutableStateFlow(DeliveryRequestState())
    val state: StateFlow<DeliveryRequestState> = _state

    fun updateStartLocation(
        address: String,
        detail: String,
        lat: Double,
        lng: Double
    ) {
        _state.update {
            it.copy(
                startAddress = address,
                startDetail = detail,
                startLat = lat,
                startLng = lng
            )
        }
    }

    fun updateDestinationLocation(
        address: String,
        detail: String,
        lat: Double,
        lng: Double
    ) {
        _state.update {
            it.copy(
                destinationAddress = address,
                destinationDetail = detail,
                destLat = lat,
                destLng = lng
            )
        }
    }

    fun updatePickupInfo(
        isImmediately: Boolean,
        date: String,
        time: String,
        isFaceToFace: Boolean,
    ) {
        _state.update {
            it.copy(
                pickupIsImmediately = isImmediately,
                pickupDate = date,
                pickupTime = time,
                pickupIsFaceToFace = isFaceToFace
            )
        }
    }

    fun updateProductInfo(
        title: String,
        description: String,
        type: String,
        weight: String,
        volume: String,
        typeLabel: String = type,
        weightLabel: String = weight,
        volumeLabel: String = volume
    ) {
        _state.update {
            it.copy(
                productTitle =  title,
                productDescription = description,
                productType = type,
                productWeight = weight,
                productVolume = volume,
                productTypeLabel = typeLabel,
                productWeightLabel = weightLabel,
                productVolumeLabel = volumeLabel
            )
        }
    }

    fun updateProductImages(imageUrls: List<String>) {
        attachImagesUrl.value = imageUrls
            .filter { it.isNotBlank() }
            .map(Uri::parse)
    }

    fun updateUser(name: String, phone: String) {
        _state.update { it.copy(name = name, phone = phone) }
    }

    fun updateDistance(distance: String){
        _state.update { it.copy(distanceKm = distance) }
    }

    fun setInternational(isInternational: Boolean) {
        _state.update {
            it.copy(isInternational = isInternational)
        }
    }

    fun clearState(){
        _state.value = DeliveryRequestState()
        attachImagesUrl.value = emptyList()
    }

    val isMapConfirmReady: StateFlow<Boolean> =
        state
            .map { s ->
                !s.startAddress.isNullOrBlank() &&
                        s.startLat != null &&
                        s.startLng != null &&

                        !s.destinationAddress.isNullOrBlank() &&
                        s.destLat != null &&
                        s.destLng != null
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    val isFeeConfirmReady: StateFlow<Boolean> =
        state
            .map { s ->
                s.startAddress != null &&
                        s.startDetail != null &&
                        s.startLat != null &&
                        s.startLng != null &&

                        s.destinationAddress != null &&
                        s.destinationDetail != null &&
                        s.destLat != null &&
                        s.destLng != null &&

                        s.pickupDate != null &&

                        s.productTitle != null &&
                        s.productDescription != null &&
                        s.productType != null &&
                        s.productWeight != null &&
                        s.productVolume != null &&

                        s.name != null &&
                        s.phone != null &&
                        s.distanceKm.isNotBlank()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )
}

data class DeliveryRequestState(
    val isInternational: Boolean = false,

    val startAddress: String? = null,
    val startDetail: String? = null,
    val startLat: Double? = null,
    val startLng: Double? = null,

    val destinationAddress: String? = null,
    val destinationDetail: String? = null,
    val destLat: Double? = null,
    val destLng: Double? = null,

    val pickupIsImmediately: Boolean = false,
    val pickupDate: String? = null, //20250912
    val pickupTime: String? = null, //1430
    val pickupIsFaceToFace: Boolean = false,

    val productTitle: String? = null,
    val productDescription: String? = null,
    val productType:  String? = null,
    val productWeight: String? = null,
    val productVolume: String? = null,
    val productTypeLabel: String? = null,
    val productWeightLabel: String? = null,
    val productVolumeLabel: String? = null,

    val name: String? = null,
    val phone: String? = null,
    val distanceKm: String = ""
)
