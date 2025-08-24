package sky.kr.co.newtogetusa.data.remote.dto.kakao

import com.google.gson.annotations.SerializedName

data class KakaoSearchAddressResponse(
    val meta: Meta,
    val documents: List<Document>
) {
    data class Meta(
        @SerializedName("total_count") val totalCount: Int,
        @SerializedName("pageable_count") val pageableCount: Int,
        @SerializedName("is_end") val isEnd: Boolean
    )

    data class Document(
        @SerializedName("address_name") val addressName: String?,
        @SerializedName("address_type") val addressType: String?, // REGION|ROAD|REGION_ADDR|ROAD_ADDR
        val x: String?, // 경도
        val y: String?, // 위도
        val address: Address?, // 지번 상세
        @SerializedName("road_address") val roadAddress: RoadAddress? // 도로명 상세
    )

    data class Address(
        @SerializedName("address_name") val addressName: String?,
        @SerializedName("region_1depth_name") val region1depthName: String?,
        @SerializedName("region_2depth_name") val region2depthName: String?,
        @SerializedName("region_3depth_name") val region3depthName: String?,
        @SerializedName("region_3depth_h_name") val region3depthHName: String?,
        @SerializedName("h_code") val hCode: String?,
        @SerializedName("b_code") val bCode: String?,
        @SerializedName("mountain_yn") val mountainYn: String?,
        @SerializedName("main_address_no") val mainAddressNo: String?,
        @SerializedName("sub_address_no") val subAddressNo: String?,
        val x: String?,
        val y: String?
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
        @SerializedName("zone_no") val zoneNo: String?,
        val x: String?,
        val y: String?
    )
}