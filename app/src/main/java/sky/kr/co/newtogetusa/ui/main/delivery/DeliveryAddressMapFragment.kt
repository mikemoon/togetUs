package sky.kr.co.newtogetusa.ui.main.delivery

import android.Manifest
import android.location.Location
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryAddressMapBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class DeliveryAddressMapFragment : BaseFragment<FragmentDeliveryAddressMapBinding, DeliveryAddressMapViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_delivery_address_map
    override val viewModel: DeliveryAddressMapViewModel by viewModels()

    private var isMapReady = false
    private var kakaoMap : KakaoMap? = null
    private var pinStyle: com.kakao.vectormap.label.LabelStyles? = null
    private var tapLabel: com.kakao.vectormap.label.Label? = null
    private var lastKnownLocation: Location? = null
    private var isFollowMode = true

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    override fun init() {
        super.init()

        dataBinding.map.start(object : MapLifeCycleCallback(){
            override fun onMapDestroy() {
                isMapReady = false
            }

            override fun onMapError(p0: Exception?) {
            }

        }, object : KakaoMapReadyCallback(){
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onMapReady(map: KakaoMap) {
                Timber.d("onMapReady")
                kakaoMap = map
                isMapReady = true

                pinStyle = kakaoMap!!.labelManager?.addLabelStyles(
                    com.kakao.vectormap.label.LabelStyles.from(
                        com.kakao.vectormap.label.LabelStyle.from(R.drawable.pin_fill_primary_png)
                    )
                )
                showMyLocation()
                kakaoMap?.setOnMapClickListener { _, latLng, _, _ ->
                    // 팔로우 자동 해제(카메라가 다시 내 위치로 튀는 것 방지)
                    kakaoMap?.trackingManager?.stopTracking()
                    isFollowMode = false

                    // 핀 찍고 카메라 이동(선택)
                    showTapPin(latLng)

                    // x=경도(lng), y=위도(lat) 주의!
                    viewModel.reverseGeocode(latLng.latitude, latLng.longitude)
                }
            }
        })
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedAddress.collect { kakaoSearchModel ->
                    //dataBinding.tvAddress.text = text ?: ""
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                is DeliveryAddressMapViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                is DeliveryAddressMapViewModel.Event.SetAddress ->{
                    viewModel.selectedAddress.value?.let {
                        val bundle = Bundle().apply {
                            putParcelable("selectedKakaoLocValue", it)
                        }
                        // 결과 전달
                        parentFragmentManager.setFragmentResult("fromC", bundle)
                    }
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun showTapPin(latLng: com.kakao.vectormap.LatLng) {
        val layer = kakaoMap?.labelManager?.layer ?: return

        // 기존 핀 제거 후 재생성 (한 개만 유지)
        tapLabel?.remove()
        tapLabel = layer.addLabel(
            LabelOptions.from(latLng).setStyles(pinStyle)
        )

        // 필요 시 카메라를 부드럽게 이동
        kakaoMap?.moveCamera(
            com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(latLng),
            CameraAnimation.from(250, true, true)
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showMyLocation() {
        if(lastKnownLocation == null){
            updateCurrentLocation {
                showStartLocation()
            }
        }else{
            showStartLocation()
        }
    }

    private fun showStartLocation(){
        lastKnownLocation?.let { loc ->
            val here = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)
            viewModel.fetchAddress(loc.latitude, loc.longitude)
            // 1) 마커(Label) 스타일/레이어
            //val styles = kakaoMap!!.labelManager
            //    ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin_fill_primary_png))) // pin 아이콘

            val layer = kakaoMap!!.labelManager?.layer

            // 2) 마커 추가 (이미 있으면 위치만 갱신)
            //val label = layer?.addLabel(LabelOptions.from(here).setStyles(styles))

            if (isFollowMode) {
                kakaoMap?.moveCamera(
                    com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here),
                    CameraAnimation.from(500, true, true)
                )
                tapLabel?.remove()
                tapLabel = layer?.addLabel(
                    LabelOptions.from(here).setStyles(pinStyle)
                )
                tapLabel?.let { kakaoMap?.trackingManager?.startTracking(it)
                    isFollowMode = false}
                kakaoMap?.trackingManager?.stopTracking()
            } else {
                // 팔로우 꺼져 있으면 혹시 모를 트래킹 종료
                kakaoMap?.trackingManager?.stopTracking()
            }
            // kakaoMap!!.trackingManager.setTrackingRotation(false) // 회전 동기화 여부
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun updateCurrentLocation(locationUpdatedCallback:()->Unit){
        val cts = CancellationTokenSource()
        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { loc ->
            if (loc != null) {
                lastKnownLocation = loc
                locationUpdatedCallback.invoke()
            }
        }.addOnFailureListener { e ->
            Timber.e("KakaoMap", "getCurrentLocation failed", e)
        }
    }
}