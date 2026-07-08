package sky.kr.co.newtogetusa.ui.main.delivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.local.model.KakaoSearchModel
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryMapBinding
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.KakaoMapSupport
import sky.kr.co.newtogetusa.utils.MapUtil.drawRouteOnKakaoMap
import timber.log.Timber
import java.lang.Exception
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.getValue

private const val ROUTE_ANIMATION_START_DELAY_MS = 700L
private const val ROUTE_ANIMATION_DURATION_MS = 2_500
private const val INITIAL_START_LOADING_TIMEOUT_MS = 10_000L

@AndroidEntryPoint
class DeliveryMapFragment : BaseFragment<FragmentDeliveryMapBinding, DeliveryMapViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_map
    override val viewModel: DeliveryMapViewModel by viewModels()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var googleMap: GoogleMap? = null
    private var googleCurrentLocationMarker: Marker? = null
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
    private var isWaitingForInitialStartLocation = false
    private var lastRouteRequestKey: String? = null

    @Inject lateinit var directionsRepo: DirectionsRepository
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private val args: DeliveryMapFragmentArgs by navArgs()

    override fun onCreateView(savedInstanceState: Bundle?) {
        super.onCreateView(savedInstanceState)
        viewModel.isInternationalDelivery.value = args.isInternational || !KakaoMapSupport.isAvailable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.isInternationalDelivery.value){
            dataBinding.googleMap.onCreate(savedInstanceState)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        super.init()
        showInitialStartLoadingIfNeeded()
        checkLocationPermission()
        if(viewModel.isInternationalDelivery.value){
            setupGoogleMap()
        }else{
            setupKakaoMap()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun setupGoogleMap(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (hasSavedStartLocation()) {
                    bindDeliveryRequestState(sharedViewModel.state.value)
                    finishInitialStartLoading()
                    return@addOnSuccessListener
                }
                location?.let {
                    lastKnownLocation = location
                    maybeInitMapWithLocation()
                } ?: finishInitialStartLoading()
            }
            .addOnFailureListener {
                finishInitialStartLoading()
            }

        //google map
        dataBinding.googleMap.getMapAsync { map ->
            googleMap = map
            googleMap?.uiSettings?.isZoomControlsEnabled = true

            map.setOnMapClickListener { latLng ->
                onMapTapped(latLng)
            }
            maybeInitMapWithLocation()
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

        dataBinding.map.start(object : MapLifeCycleCallback(){
            override fun onMapDestroy() {
                isMapReady = false
                lastRouteRequestKey = null
            }

            override fun onMapError(p0: Exception?) {
                finishInitialStartLoading()
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
                bindDeliveryRequestState(sharedViewModel.state.value)
                if (hasSavedStartLocation()) {
                    finishInitialStartLoading()
                } else {
                    showMyLocation()
                }

                applyPendingIfAny()
            }
        })
    }

    private fun maybeInitMapWithLocation() {
        if (googleMap != null && lastKnownLocation != null) {
            val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
            showGoogleCurrentLocation(lastKnownLocation!!)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            finishInitialStartLoading()
        }
        if (lastKnownLocation == null) {//서울
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.5665, 126.9780), 12f))
            finishInitialStartLoading()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showMyLocation() {
        if (hasSavedStartLocation()) {
            bindDeliveryRequestState(sharedViewModel.state.value)
            finishInitialStartLoading()
            return
        }
        if(lastKnownLocation == null){
            updateCurrentLocation {
                showStartLocation()
            }
        }else{
            showStartLocation()
        }
    }

    private fun onMapTapped(latLng: LatLng) {
        viewLifecycleOwner.lifecycleScope.launch {
            val addr = reverseGeocode(requireContext(), latLng.latitude, latLng.longitude)
            val title = addr?.let { it.getAddressLine(0) ?: "${it.adminArea ?: ""} ${it.locality ?: ""}".trim() }
                ?: "${latLng.latitude}, ${latLng.longitude}"

            // 마커 갱신
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title(title))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            //if(viewModel.address.value.isNullOrEmpty())
            //viewModel.setStartAddress(title)
            // 뷰모델/UI 반영 (예: 도착지 주소에 세팅)

            viewModel.setDestinationAddress(title)
            // 필요하면 좌표도 보관
            // viewModel.setDestinationLatLng(latLng.latitude, latLng.longitude)
        }
    }

    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): Address? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 비동기 API
            suspendCancellableCoroutine { cont ->
                val geocoder = Geocoder(context, Locale.getDefault())
                geocoder.getFromLocation(lat, lng, 1) { list ->
                    cont.resume(list?.firstOrNull())
                }
            }
        } else {
            // 구버전 동기 API는 IO 스레드에서
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                } catch (e: Exception) {
                    null
                }
            }
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
            } else {
                finishInitialStartLoading()
            }
        }.addOnFailureListener { e ->
            Timber.e("KakaoMap getCurrentLocation failed $e")
            finishInitialStartLoading()
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
        bindDeliveryRequestState(sharedViewModel.state.value)
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

    private fun showStartLocation(){
        if (hasSavedStartLocation()) {
            bindDeliveryRequestState(sharedViewModel.state.value)
            finishInitialStartLoading()
            return
        }
        val loc = lastKnownLocation ?: run {
            finishInitialStartLoading()
            return
        }
        val map = kakaoMap ?: return
        val here = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)
        viewModel.fetchAddress(loc.latitude, loc.longitude)
        // 1) 마커(Label) 스타일/레이어
        val styles = map.labelManager
            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin_depart))) // pin 아이콘

        val layer = map.labelManager?.layer

        // 2) 마커 추가 (이미 있으면 위치만 갱신)
        layer?.addLabel(LabelOptions.from(here).setStyles(styles))

        if (isFollowMode) {
            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here)
            )
            map.trackingManager?.stopTracking()
        } else {
            // 팔로우 꺼져 있으면 혹시 모를 트래킹 종료
            map.trackingManager?.stopTracking()
        }
        finishInitialStartLoading()
        // kakaoMap!!.trackingManager.setTrackingRotation(false) // 회전 동기화 여부
    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.address.collectLatest { addr ->
                    val currentLocation = lastKnownLocation ?: return@collectLatest
                    val currentState = sharedViewModel.state.value
                    if (
                        addr.isUsableStartAddress() &&
                        currentState.startLat == null &&
                        currentState.startLng == null
                    ) {
                        sharedViewModel.updateStartLocation(
                            address = addr.orEmpty(),
                            detail = currentState.startDetail.orEmpty(),
                            lat = currentLocation.latitude,
                            lng = currentLocation.longitude
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                sharedViewModel.state.collectLatest { state ->
                    bindDeliveryRequestState(state)
                }
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
                    saveCurrentStartLocationIfAvailable()
                    findNavController().navigate(DeliveryMapFragmentDirections.actionDeliveryMapFragmentToDeliveryStartFragment2(isStart = true, isInternational = viewModel.isInternationalDelivery.value))
                }
                DeliveryMapViewModel.Event.SelectDestination ->{
                    findNavController().navigate(DeliveryMapFragmentDirections.actionDeliveryMapFragmentToDeliveryStartFragment2(isStart = false, isInternational = viewModel.isInternationalDelivery.value))
                    //findNavController().navigate(DeliveryMapFragmentDirections.actionDeliveryMapFragmentToDeliverySearchFragment(isStart = false, isInternational = viewModel.isInternationalDelivery.value))
                }
                DeliveryMapViewModel.Event.Confirm ->{
                    setFragmentResult()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun bindDeliveryRequestState(state: DeliveryRequestState) {
        viewModel.updateConfirmButtonEnable(state.hasRequiredRouteLocations())

        if (!state.startAddress.isNullOrBlank()) {
            viewModel.setStartAddress(state.startAddress)
            viewModel.addressDetail.value = state.startDetail.orEmpty()
        }

        if (state.startLat != null && state.startLng != null) {
            val startLocation = Location("kakao").apply {
                latitude = state.startLat
                longitude = state.startLng
            }
            lastKnownLocation = startLocation

            if (isMapReady) applyStartLocation(startLocation)
            else queuedStart = startLocation
        }

        if (!state.destinationAddress.isNullOrBlank()) {
            viewModel.setDestinationAddress(state.destinationAddress)
            viewModel.destinationDetailAddress.value = state.destinationDetail.orEmpty()
        }

        if (state.destLat != null && state.destLng != null) {
            val destLocation = Location("kakao").apply {
                latitude = state.destLat
                longitude = state.destLng
            }
            lastKnownDestLocation = destLocation

            if (isMapReady) applyDestLocation(destLocation)
            else queuedDest = destLocation
        }

        val start = lastKnownLocation
        val end = lastKnownDestLocation
        if (start != null && end != null) {
            requestRouteDraw(start, end)
        } else {
            lastRouteRequestKey = null
        }
    }

    private fun setFragmentResult(){
        sharedViewModel.updateDistance(
            distance = calcDistanceKm(lastKnownLocation, lastKnownDestLocation).toString()
        )
    }

    private fun saveCurrentStartLocationIfAvailable() {
        val currentState = sharedViewModel.state.value
        if (currentState.startLat != null && currentState.startLng != null) return

        val location = lastKnownLocation ?: return
        val address = viewModel.address.value
        if (!address.isUsableStartAddress()) return

        sharedViewModel.updateStartLocation(
            address = address.orEmpty(),
            detail = currentState.startDetail.orEmpty(),
            lat = location.latitude,
            lng = location.longitude
        )
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
            requestRouteDraw(s, d)    // 아래 5) 참고
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
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here)
            )
            map.trackingManager?.stopTracking()
        } else {
            map.trackingManager?.stopTracking()
        }
        finishInitialStartLoading()
    }

    private fun showInitialStartLoadingIfNeeded() {
        if (!shouldWaitForInitialStartLocation()) {
            finishInitialStartLoading()
            return
        }

        isWaitingForInitialStartLocation = true
        dataBinding.initialStartLoadingOverlay.isVisible = true

        viewLifecycleOwner.lifecycleScope.launch {
            delay(INITIAL_START_LOADING_TIMEOUT_MS)
            if (isWaitingForInitialStartLocation) {
                finishInitialStartLoading()
            }
        }
    }

    private fun shouldWaitForInitialStartLocation(): Boolean {
        val state = sharedViewModel.state.value
        return state.startAddress.isNullOrBlank() &&
            state.startLat == null &&
            state.startLng == null
    }

    private fun hasSavedStartLocation(): Boolean {
        val state = sharedViewModel.state.value
        return !state.startAddress.isNullOrBlank() &&
            state.startLat != null &&
            state.startLng != null
    }

    private fun finishInitialStartLoading() {
        isWaitingForInitialStartLocation = false
        dataBinding.initialStartLoadingOverlay.isVisible = false
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

    private fun requestRouteDraw(start: Location, dest: Location) {
        if (!isValidRouteLocation(start) || !isValidRouteLocation(dest)) {
            Timber.w("skip route draw invalid location start=$start dest=$dest")
            return
        }

        if (!isMapReady || kakaoMap == null) {
            queuedRoute = start to dest
            return
        }

        val routeKey = "${start.latitude},${start.longitude}-${dest.latitude},${dest.longitude}"
        if (lastRouteRequestKey == routeKey) return
        lastRouteRequestKey = routeKey
        drawRouteFromTo(start, dest)
    }

    private fun drawRouteFromTo(start: Location, dest: Location) {
        val map = kakaoMap ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val startLL = com.kakao.vectormap.LatLng.from(start.latitude, start.longitude)
            val destLL  = com.kakao.vectormap.LatLng.from(dest.latitude,  dest.longitude)
            val fallbackPoints = listOf(startLL, destLL)
            val fallbackDistance = calcDistanceMeters(start, dest)

            disableFollowMode()
            upsertPins(startLL, destLL, null)

            fitRouteAndAdjustZoom(
                map = map,
                points = fallbackPoints,
                startLL = startLL,
                destLL = destLL,
                distanceMeters = fallbackDistance,
                paddingDp = 160
            )

            val (points, summary) = runCatching {
                directionsRepo.fetchRoute(
                    startLat = start.latitude, startLng = start.longitude,
                    endLat = dest.latitude,   endLng = dest.longitude
                )
            }.onFailure {
                Timber.e(it, "drawRouteFromTo fetchRoute failed")
            }.getOrDefault(emptyList<com.kakao.vectormap.LatLng>() to null)

            val routePoints = points.takeIf { it.isNotEmpty() } ?: fallbackPoints
            Timber.d("drawRouteFromTo points: ${routePoints.size}, summary: $summary")

            fitRouteAndAdjustZoom(
                map = map,
                points = routePoints,
                startLL = startLL,
                destLL = destLL,
                distanceMeters = summary?.distance ?: fallbackDistance,
                paddingDp = 160
            )

            delay(ROUTE_ANIMATION_START_DELAY_MS)
            drawRouteOnKakaoMap(
                kakaoMap = map,
                points = routePoints,
                summary = summary,
                moveCamera = false,
                clearPrevious = true,
                animate = true,
                animationDurationMillis = ROUTE_ANIMATION_DURATION_MS
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
        startLabel?.position?.let { position ->
            kakaoMap?.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(position)
            )
        }
        kakaoMap?.trackingManager?.stopTracking()
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
            googleMap?.isMyLocationEnabled = false
            lastKnownLocation?.let { showGoogleCurrentLocation(it) }
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
                finishInitialStartLoading()
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
    /** 경로와 핀을 지도 레이어 안에 모두 포함시킨다. */
    private fun fitRouteAndAdjustZoom(
        map: com.kakao.vectormap.KakaoMap,
        points: List<com.kakao.vectormap.LatLng>,
        startLL: com.kakao.vectormap.LatLng,
        destLL:  com.kakao.vectormap.LatLng,
        distanceMeters: Int?,
        paddingDp: Int = 96
    ) {
        val bb = com.kakao.vectormap.LatLngBounds.Builder()
        bb.include(startLL)
        bb.include(destLL)
        points.forEach { bb.include(it) }
        val bounds = bb.build()
        val routePaddingDp = routeFitPaddingDp(distanceMeters).coerceAtMost(paddingDp)

        map.moveCamera(
            com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bounds, dp(routePaddingDp))
        )
    }

    private fun routeFitPaddingDp(distanceMeters: Int?): Int = when {
        distanceMeters == null -> 96
        distanceMeters < 1_000 -> 32
        distanceMeters < 3_000 -> 48
        distanceMeters < 7_000 -> 64
        distanceMeters < 15_000 -> 80
        else -> 120
    }

    fun calcDistanceKm(
        start: Location?,
        end: Location?
    ): Double {
        if (start == null || end == null) return 0.0

        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            result
        )
        return result[0] / 1000.0 // meter → km
    }

    private fun calcDistanceMeters(start: Location, end: Location): Int {
        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            result
        )
        return result[0].toInt()
    }

    private fun isValidRouteLocation(location: Location): Boolean {
        return location.latitude != 0.0 || location.longitude != 0.0
    }

    private fun dp(px: Int) = (px * resources.displayMetrics.density + 0.5f).toInt()

    private fun DeliveryRequestState.hasRequiredRouteLocations(): Boolean {
        return !startAddress.isNullOrBlank() &&
            startLat != null &&
            startLng != null &&
            !destinationAddress.isNullOrBlank() &&
            destLat != null &&
            destLng != null
    }

    private fun String?.isUsableStartAddress(): Boolean {
        return !isNullOrBlank() && this != "출발지 선택"
    }
}
