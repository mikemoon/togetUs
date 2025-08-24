package sky.kr.co.newtogetusa.data.remote.dto.kakao

import com.google.gson.annotations.SerializedName

data class KakaoCoord2AddressResponse(
    val meta: Meta,
    val documents: List<Document>
) {
    data class Meta(
        @SerializedName("total_count") val totalCount: Int
    )

    data class Document(
        val address: Address?,                                     // 지번 주소
        @SerializedName("road_address") val roadAddress: RoadAddress? // 도로명 주소(없을 수 있음)
    )

    data class Address(
        @SerializedName("address_name") val addressName: String?,
        @SerializedName("region_1depth_name") val region1depthName: String?,
        @SerializedName("region_2depth_name") val region2depthName: String?,
        @SerializedName("region_3depth_name") val region3depthName: String?,
        @SerializedName("mountain_yn") val mountainYn: String?,
        @SerializedName("main_address_no") val mainAddressNo: String?,
        @SerializedName("sub_address_no") val subAddressNo: String?
        // zip_code는 Deprecated라 의도적으로 제외
    )

    data class RoadAddress(
        @SerializedName("address_name") val addressName: String?,
        @SerializedName("region_1depth_name") val region1depthName: String?,
        @SerializedName("region_2depth_name") val region2depthName: String?,
        @SerializedName("region_3depth_name") val region3depthName: String?,
        @SerializedName("road_name") val roadName: String?,
        @SerializedName("underground_yn") val undergroundYn: String?,
        @SerializedName("main_building_no") val mainBuildingNo: String?,
        @SerializedName("sub_building_no") val subBuildingNo: String?,
        @SerializedName("building_name") val buildingName: String?,
        @SerializedName("zone_no") val zoneNo: String?
    )
}