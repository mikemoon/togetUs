package sky.kr.co.newtogetusa.ui.main.delivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryMapBinding
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.MapUtil.drawRouteOnKakaoMap
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class DeliveryMapFragment : BaseFragment<FragmentDeliveryMapBinding, DeliveryMapViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_map
    override val viewModel: DeliveryMapViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private var lastKnownDestLocation : Location? = null

    private var startAddress: SearchResultModel? = null
    private var endAddress: SearchResultModel? = null

    private var isFollowMode: Boolean = true
    private var kakaoMap : KakaoMap? = null
    private var startLatLng: LatLng? = null
    private var endLatLng: LatLng? = null
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null
    private var startStyles: com.kakao.vectormap.label.LabelStyles? = null
    private var destStyles: com.kakao.vectormap.label.LabelStyles? = null

    // 상태
    private var isMapReady = false

    // onMapReady 전에 도착한 요청 대기
    private var queuedStart: Location? = null
    private var queuedDest: Location? = null
    private var queuedRoute: Pair<Location, Location>? = null

    @Inject lateinit var directionsRepo: DirectionsRepository
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    override fun onCreateView(savedInstanceState: Bundle?) {
        super.onCreateView(savedInstanceState)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()
        checkLocationPermission()
        /*val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    lastKnownLocation = location
                    maybeInitMapWithLocation()
                }
            }*/

        dataBinding.map.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    if (isFollowMode) disableFollowMode()
                }
            }
            false // 지도 터치는 계속 처리되도록 false
        }

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
                Timber.d("mapReady queRoute: $queuedRoute")
                showMyLocation()

                applyPendingIfAny()
            }
        })

        //google map
        /*dataBinding.map.getMapAsync { map ->
            googleMap = map
            googleMap?.uiSettings?.isZoomControlsEnabled = true
            maybeInitMapWithLocation()
        }*/
    }

    private fun maybeInitMapWithLocation() {
        if (googleMap != null && lastKnownLocation != null) {
            val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
            googleMap?.addMarker(MarkerOptions().position(latLng).title("내 위치"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
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

    override fun onStart() {
        super.onStart()
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        dataBinding.map.resume()
        //showStartLocation()
    }

    override fun onPause() {
        super.onPause()
        dataBinding.map.pause()
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        //dataBinding.map.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //dataBinding.map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        //dataBinding.map.onLowMemory()
    }

    private fun showStartLocation(){
        lastKnownLocation?.let { loc ->
            val here = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)
            viewModel.fetchAddress(loc.latitude, loc.longitude)
            // 1) 마커(Label) 스타일/레이어
            val styles = kakaoMap!!.labelManager
                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin_depart))) // pin 아이콘

            val layer = kakaoMap!!.labelManager?.layer

            // 2) 마커 추가 (이미 있으면 위치만 갱신)
            val label = layer?.addLabel(LabelOptions.from(here).setStyles(styles))

            if (isFollowMode) {
                kakaoMap?.moveCamera(
                    com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here),
                    CameraAnimation.from(500, true, true)
                )
                // 팔로우 모드일 때만 트래킹 시작
                startLabel?.let { kakaoMap?.trackingManager?.startTracking(it) }
            } else {
                // 팔로우 꺼져 있으면 혹시 모를 트래킹 종료
                kakaoMap?.trackingManager?.stopTracking()
            }
            // kakaoMap!!.trackingManager.setTrackingRotation(false) // 회전 동기화 여부
        }
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.address.collectLatest { addr ->

                }
            }
        }

        parentFragmentManager.setFragmentResultListener("fromB", viewLifecycleOwner) { requestKey, bundle ->
            val isStartLoc = bundle.getBoolean("isStart")
            val result = bundle.getParcelable<KakaoSearchModel>("selectedKakaoLocValue") ?: return@setFragmentResultListener

            val loc = Location("kakao").apply {
                latitude = result.lat ?: return@setFragmentResultListener
                longitude = result.lng ?: return@setFragmentResultListener
            }
            Timber.d("kakaoLocSelected $isStartLoc, $result, $isMapReady")

            val addressName = if(result.roadAddress.isNullOrEmpty()) result.name else result.roadAddress
            if (isStartLoc) {
                lastKnownLocation = loc // 상태 갱신
                viewModel.setStartAddress(addressName)
                if (isMapReady) applyStartLocation(loc) else queuedStart = loc
            } else {
                lastKnownDestLocation = loc
                viewModel.setDestinationAddress(addressName)
                if (isMapReady) applyDestLocation(loc, result.name) else queuedDest = loc
            }

            // 경로는 시작/도착이 모두 있을 때 큐 or 즉시
            val start = lastKnownLocation
            val end = lastKnownDestLocation
            if (start != null && end != null) {
                if (isMapReady) drawRouteFromTo(start, end)
                else queuedRoute = start to loc
            }

        }

        viewModel.route.observe(viewLifecycleOwner){
            if(it.isNotEmpty())
            drawRouteOnMap(googleMap!!, it)
        }

        viewModel.event.observe(viewLifecycleOwner){ event ->
            when(event){
                DeliveryMapViewModel.Event.Back ->{
                    findNavController().popBackStack()
                }
                DeliveryMapViewModel.Event.SelectStart -> {
                    findNavController().navigate(DeliveryMapFragmentDirections.actionDeliveryMapFragmentToDeliveryStartFragment2(isStart = true))
                }
                DeliveryMapViewModel.Event.SelectDestination ->{
                    findNavController().navigate(DeliveryMapFragmentDirections.actionDeliveryMapFragmentToDeliveryStartFragment2(isStart = false))
                }
            }
        }
    }

    private fun applyPendingIfAny() {
        if (!isMapReady) return
        queuedStart?.let {
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
        }
    }

    private fun applyStartLocation(loc: Location) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        val here = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)

        // 라벨 갱신은 필드에!
        startLabel?.remove()
        startLabel = layer.addLabel(
            LabelOptions.from(here).setStyles(startStyles)
        )

        if (isFollowMode) {
            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here),
                CameraAnimation.from(500, true, true)
            )
            startLabel?.let { map.trackingManager?.startTracking(it) }
        } else {
            map.trackingManager?.stopTracking()
        }
    }

    private fun applyDestLocation(loc: Location, name: String? = null) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        val dest = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)

        destLabel?.remove()
        destLabel = layer.addLabel(
            LabelOptions.from(dest).setStyles(destStyles)
        )
    }

    private fun drawRouteFromTo(start: Location, dest: Location) {
        val map = kakaoMap ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val (points, summary) = directionsRepo.fetchRoute(
                startLat = start.latitude, startLng = start.longitude,
                endLat = dest.latitude,   endLng = dest.longitude
            )
            Timber.d("drawRouteFromTo points: $points, summary: $summary")
            if (points.isEmpty()) {
                // 필요 시 기존 라인/핀 정리
                return@launch
            }
            disableFollowMode()
            drawRouteOnKakaoMap(map, points, summary)

            val startLL = com.kakao.vectormap.LatLng.from(start.latitude, start.longitude)
            val destLL  = com.kakao.vectormap.LatLng.from(dest.latitude,  dest.longitude)
            upsertPins(startLL, destLL, null)

            //val bb = com.kakao.vectormap.LatLngBounds.Builder()
            //points.forEach { bb.include(it) }
            //bb.include(startLL)
            //bb.include(destLL)
            //val bounds = bb.build()
            //if (points.isNotEmpty()) points.forEach { b.include(it) } else { b.include(startLL); b.include(destLL) }
            //map.moveCamera(com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bounds, 96))

            fitRouteAndAdjustZoom(
                map = map,
                points = points,
                startLL = startLL,
                destLL = destLL,
                distanceMeters = summary?.distance,
                paddingDp = 160,  // 바텀시트/상단바 있으면 120~160까지 늘려도 좋음
                minZoom = 8f,
                maxZoom = 18f
            )
        }
    }

    private fun upsertPins(
        start: com.kakao.vectormap.LatLng?,
        dest: com.kakao.vectormap.LatLng?,
        destName: String? = null
    ) {
        val lm = kakaoMap?.labelManager ?: return
        val layer = lm.layer ?: return

        // 출발 핀
        start?.let {
            // 기존 라벨이 있으면 제거 후 다시 추가(가장 안전)
            startLabel?.remove()
            startLabel = layer.addLabel(
                com.kakao.vectormap.label.LabelOptions
                    .from(it)
                    .setStyles(startStyles)
            )
        }

        // 도착 핀
        dest?.let {
            destLabel?.remove()
            destLabel = layer.addLabel(
                com.kakao.vectormap.label.LabelOptions
                    .from(it)
                    .setStyles(destStyles)
            )
        }
    }

    private fun enableFollowMode() {
        isFollowMode = true
        // 현재 라벨이 있으면 다시 추적 시작
        startLabel?.let { kakaoMap?.trackingManager?.startTracking(it) }
    }

    private fun disableFollowMode() {
        isFollowMode = false
        kakaoMap?.trackingManager?.stopTracking()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 이미 허용됨
            enableMyLocation()
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // 내 위치 활성화 (GoogleMap 객체가 초기화된 후 호출)
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한 허용됨
                enableMyLocation()
            } else {
                // 권한 거부됨
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun drawRouteOnMap(googleMap: GoogleMap, points: List<LatLng>) {
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .color(Color.BLUE)
            .width(10f)

        googleMap.addPolyline(polylineOptions)

        if (points.isNotEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 12f))
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dLng

            val latLng = LatLng(lat / 1E5, lng / 1E5)
            poly.add(latLng)
        }

        return poly
    }

    //kakao
    private fun preferredZoomFor(distanceMeters: Int): Float = when {
        distanceMeters < 1_000   -> 15f
        distanceMeters < 3_000   -> 14f
        distanceMeters < 7_000   -> 13f
        distanceMeters < 15_000  -> 12f
        distanceMeters < 30_000  -> 11f
        else                     -> 10f
    }

    /** 경로와 핀을 모두 포함시키고, 선호 줌으로 한 단계 조정 */
    private fun fitRouteAndAdjustZoom(
        map: com.kakao.vectormap.KakaoMap,
        points: List<com.kakao.vectormap.LatLng>,
        startLL: com.kakao.vectormap.LatLng,
        destLL:  com.kakao.vectormap.LatLng,
        distanceMeters: Int?,               // Kakao summary.distance
        paddingDp: Int = 96,
        minZoom: Float = 10f,
        maxZoom: Float = 18f
    ) {
        // 1) bounds 구성
        val bb = com.kakao.vectormap.LatLngBounds.Builder()
        if (points.isNotEmpty()) points.forEach { bb.include(it) } else { bb.include(startLL); bb.include(destLL) }
        val bounds = bb.build()

        // 2) 먼저 fit (둘 다 화면에 보이게)
        map.moveCamera(
            com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bounds, dp(paddingDp))
        )

        // 3) 경로 길이에 따라 원하는 줌 레벨로 ‘한 번 더’ 세팅 (너무 가까우면 한 단계 낮추는 느낌)
        distanceMeters?.let {
            Timber.d("preferredZoomFor $distanceMeters")
            val target = preferredZoomFor(it).coerceIn(minZoom, maxZoom)
            map.moveCamera(com.kakao.vectormap.camera.CameraUpdateFactory.zoomTo(target.toInt()))
        }
    }

    private fun dp(px: Int) = (px * resources.displayMetrics.density + 0.5f).toInt()
}