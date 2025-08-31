package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.data.remote.ResultWrapper
import sky.kr.co.newtogetusa.data.remote.dto.search.RegionDto
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomAreaSelectViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val configRepository: ConfigRepository): BaseViewModel(baseViewModelFactory.create()) {

    val regions = listOf("전체", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "강원", "경기", "경남", "경북", "전남", "전북", "제주", "충남", "충북")
    val detailRegions = listOf("전체", "광산구", "남구", "동구", "북구", "서구")

    val foreignRegions = listOf("전체", "일본", "미국", "베트남", "싱가포르", "태국", "캐나다", "독일", "영국", "프랑스", "UAE", "호주")
    val foreignDetailRegions = listOf("전체", "교토", "나고야", "도쿄", "샷포로", "오사카", "후쿠오카")

    val isLocal = MutableStateFlow(true)

    fun onAreaSelect(isLocalArea: Boolean){
        isLocal.value = isLocalArea
    }

    val domesticAddressList = MutableStateFlow<List<RegionDto>>(emptyList())
    fun getDomesticAddressList() = viewModelScope.launch {
        val response = configRepository.getDomesticAreas()
        when(response){
            is ResultWrapper.Success ->{
                domesticAddressList.value = response.data
            }
            else -> {}
        }
    }

    val domesticSubAddressList = MutableStateFlow<List<RegionDto>>(emptyList())
    fun getDomesticSubAddressList() = viewModelScope.launch {
        val response = configRepository.getDomesticSubAreas()
        when(response){
            is ResultWrapper.Success ->{
                domesticSubAddressList.value = response.data
            }
            else -> {}
        }
    }
}