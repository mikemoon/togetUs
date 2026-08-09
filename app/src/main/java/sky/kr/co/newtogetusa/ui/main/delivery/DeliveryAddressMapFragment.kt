package sky.kr.co.newtogetusa.ui.main.delivery

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.GoogleMapSearchModel
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryAddressMapBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.KakaoMapSupport
import timber.log.Timber
import java.lang.Exception
import java.util.Locale
import kotlin.coroutines.resume

@AndroidEntryPoint
class DeliveryAddressMapFragment : BaseFragment<FragmentDeliveryAddressMapBinding, DeliveryAddressMapViewModel>(){
    override val layoutId: Int
        get() = R.layout.fragment_delivery_address_map
    override val viewModel: DeliveryAddressMapViewModel by viewModels()

    private val args: DeliveryAddressMapFragmentArgs by navArgs()

    private var isMapReady = false
    private var kakaoMap : KakaoMap? = null
    private var pinStyle: com.kakao.vectormap.label.LabelStyles? = null
    private var tapLabel: com.kakao.vectormap.label.Label? = null
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null
    private var startStyles: com.kakao.vectormap.label.LabelStyles? = null
    private var destStyles: com.kakao.vectormap.label.LabelStyles? = null
    private var lastKnownLocation: Location? = null
    private var isFollowMode = true

    private var googleMap: GoogleMap? = null
    private var googleCurrentLocationMarker: Marker? = null

    // 검색 화면에서 전달된 초기 주소 (있으면 내 위치 대신 해당 위치 중심으로 표시)
    private val initialLatLng: LatLng? by lazy {
        val address = arguments?.getParcelable<KakaoSearchModel>("initialAddress")
        val lat = address?.lat
        val lng = address?.lng
        if (lat != null && lng != null && lat > 0 && lng > 0) LatLng(lat, lng) else null
    }

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun init() {
        super.init()

        viewModel.isInternationalDelivery.value = args.isInternational || !KakaoMapSupport.isAvailable

        if(viewModel.isInternationalDelivery.value){
            setupGoogleMap()
        }else{
            setupKakaoMap()
        }
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
                            putBoolean("isStart", args.isStart)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onStart()
        }
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onResume()
        }else {
            dataBinding.map.resume()
        }
        //showStartLocation()
    }

    override fun onPause() {
        super.onPause()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onPause()
        }else {
            dataBinding.map.pause()
        }
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onStop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onLowMemory()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupGoogleMap(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    lastKnownLocation = location
                    maybeInitMapWithLocation()
                }
            }

        //google map
        dataBinding.googleMap.getMapAsync { map ->
            googleMap = map
            googleMap?.uiSettings?.isZoomControlsEnabled = true

            map.setOnMapClickListener { latLng ->
                onMapTapped(latLng)
            }

            val initial = initialLatLng
            if (initial != null) {
                // 검색 화면에서 선택한 주소 위치로 마커 + 카메라 이동
                onMapTapped(initial)
            } else {
                maybeInitMapWithLocation()
            }
        }
    }

    private fun maybeInitMapWithLocation() {
        if (initialLatLng != null) return
        if (googleMap != null && lastKnownLocation != null) {
            val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
            showGoogleCurrentLocation(lastKnownLocation!!)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        if (lastKnownLocation == null) {//서울
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.5665, 126.9780), 12f))
        }
    }

    private fun setupKakaoMap(){
        dataBinding.map.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (isFollowMode) disableFollowMode()
                }
            }
            false // 지도 터치는 계속 처리되도록 false
        }

        /*dataBinding.map.start(object : MapLifeCycleCallback(){
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

                startStyles = kakaoMap!!.labelManager?.addLabelStyles(
                    com.kakao.vectormap.label.LabelStyles.from(
                        com.kakao.vectormap.label.LabelStyle.from(R.drawable.pin_depart)
                    )
                )
                destStyles = kakaoMap!!.labelManager?.addLabelStyles(
                    com.kakao.vectormap.label.LabelStyles.from(
                        com.kakao.vectormap.label.LabelStyle.from(R.drawable.pin_arrive)
                    )
                )
                Timber.d("mapReady showMyLocation, lastKnownLocation: $lastKnownLocation")
                //Timber.d("mapReady queRoute: $queuedRoute")
                showMyLocation()

                applyPendingIfAny()
            }
        })*/

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
                    LabelStyles.from(
                        LabelStyle.from(R.drawable.pin_fill_primary_png)
                    )
                )
                val initial = initialLatLng
                if (initial != null) {
                    // 검색 화면에서 선택한 주소 위치로 핀 + 카메라 이동
                    kakaoMap?.trackingManager?.stopTracking()
                    isFollowMode = false
                    val latLng = com.kakao.vectormap.LatLng.from(initial.latitude, initial.longitude)
                    showTapPin(latLng)
                    viewModel.reverseGeocode(latLng.latitude, latLng.longitude)
                } else {
                    showMyLocation()
                }
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

    private fun disableFollowMode() {
        isFollowMode = false
        kakaoMap?.trackingManager?.stopTracking()
    }

    private fun onMapTapped(latLng: LatLng) {
        // 1) 먼저 마커 표시
        googleMap?.clear()
        val marker = googleMap?.addMarker(
            MarkerOptions().position(latLng).title("${latLng.latitude}, ${latLng.longitude}")
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // 2) 비동기로 역지오코딩
        viewLifecycleOwner.lifecycleScope.launch {
            val addr = reverseGeocode(requireContext(), latLng.latitude, latLng.longitude)
            val title = addr?.getAddressLine(0)
                ?: listOfNotNull(addr?.adminArea, addr?.locality).joinToString(" ").ifBlank {
                    "${latLng.latitude}, ${latLng.longitude}"
                }

            marker?.title = title
            marker?.showInfoWindow()

            viewModel.selectedAddressByGoogleMap.value = GoogleMapSearchModel(
                name = title, lng = latLng.longitude, lat = latLng.latitude
            )
            viewModel.uiSelectedAddressName.value = title
        }
    }

    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): Address? {
        // 일부 단말/이미지에서는 Geocoder backend 자체가 없음
        if (!Geocoder.isPresent()) {
            Timber.w("Geocoder backend is not present on this device")
            return null
        }

        return withTimeoutOrNull(3000) { // 3초 타임아웃
            suspendCancellableCoroutine { cont ->
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (!cont.isCompleted) cont.resume(addresses.firstOrNull())
                        }
                        override fun onError(errorMessage: String?) {
                            Timber.w("Geocoder error: $errorMessage")
                            if (!cont.isCompleted) cont.resume(null)
                        }
                    })
                } else {
                    // 동기 API는 IO 스레드에서
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            @Suppress("DEPRECATION")
                            val list = geocoder.getFromLocation(lat, lng, 1)
                            if (!cont.isCompleted) cont.resume(list?.firstOrNull())
                        } catch (e: Exception) {
                            Timber.w(e, "Geocoder getFromLocation failed")
                            if (!cont.isCompleted) cont.resume(null)
                        }
                    }
                }
            }
        }
    }

    private fun applyPendingIfAny() {
        if (!isMapReady) return
        /*queuedStart?.let {
            applyStartLocation(it)   // 아래 3) 참고
            queuedStart = null
        }
        queuedDest?.let {
            applyDestLocation(it)
            queuedDest = null
        }
        queuedRoute?.let { (s, d) ->
            drawRouteFromTo(s, d)    // 아래 5) 참고
            queuedRoute = null
        }*/
    }

    private fun showTapPin(latLng: com.kakao.vectormap.LatLng) {
        val layer = kakaoMap?.labelManager?.layer ?: return

        // 기존 핀 제거 후 재생성 (한 개만 유지)
        tapLabel?.remove()
        tapLabel = layer.addLabel(
            LabelOptions.from(latLng).setStyles(pinStyle)
        )

        kakaoMap?.moveCamera(
            com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(latLng)
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
                    com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here)
                )
                tapLabel?.remove()
                tapLabel = layer?.addLabel(
                    LabelOptions.from(here).setStyles(pinStyle)
                )
                kakaoMap?.trackingManager?.stopTracking()
                isFollowMode = false
            } else {
                // 팔로우 꺼져 있으면 혹시 모를 트래킹 종료
                kakaoMap?.trackingManager?.stopTracking()
            }
            // kakaoMap!!.trackingManager.setTrackingRotation(false) // 회전 동기화 여부
        }
    }

    private fun showGoogleCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleCurrentLocationMarker?.remove()
        googleCurrentLocationMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("내 위치")
                .anchor(0.5f, 1f)
        )
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
