package sky.kr.co.newtogetusa.data.remote.request.delivery

import com.google.gson.annotations.SerializedName

data class DeliveryRequest(
    val title: String,
    val is_domestic: Boolean,
    val depart: DepartInfo,
    val depart_contact: ContactInfo,
    val dest: DestInfo,
    val dest_contact: ContactInfo?,
    val pickup: PickupInfo,
    val product: ProductInfo
)

data class DepartInfo(
    val address: String,
    val address2: String?,
    val latitude: Double,
    val longitude: Double
)

data class DestInfo(
    val address: String,
    val address2: String?,
    val latitude: Double,
    val longitude: Double
)
data class ContactInfo(
    val name: String?,
    val phone: String?
)

data class PickupInfo(
    val is_immediately: Boolean,
    val date: String,   // "20250912"
    val time: String,   // "1430"
    val is_face2face: Boolean
)

data class ProductInfo(
    val name: String,
    val type_cd: String,
    val weight_cd: String,
    val volume_cd: String,
    val descript: String
)