package sky.kr.co.newtogetusa.ui.main.history

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentMapDetailBinding
import sky.kr.co.newtogetusa.repository.DirectionsRepository
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.home.HomeTabViewModel
import sky.kr.co.newtogetusa.utils.MapUtil.drawRouteOnKakaoMap
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class MapDetailFragment : BaseFragment<FragmentMapDetailBinding, MapDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_map_detail
    override val viewModel: MapDetailViewModel by viewModels()

    private val args: MapDetailFragmentArgs by navArgs()

    private var isMapReady = false
    private var kakaoMap: KakaoMap? = null
    private var googleMap: GoogleMap? = null
    private var startStyles: LabelStyles? = null
    private var destStyles: LabelStyles? = null
    private var startLabel: com.kakao.vectormap.label.Label? = null
    private var destLabel: com.kakao.vectormap.label.Label? = null

    // 플레이어 GPS 마커 (iOS: 국내/카카오맵에서만 표시)
    private var playerLabel: com.kakao.vectormap.label.Label? = null
    private var playerStyles: LabelStyles? = null
    private var playerBitmap: android.graphics.Bitmap? = null
    private var playerBitmapLoaded = false

    @Inject
    lateinit var directionsRepo: DirectionsRepository

    override fun init() {
        super.init()

        dataBinding.tvStartAddress.text = args.startAddress
        dataBinding.tvEndAddress.text = args.endAddress

        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            setupGoogleMap()
        } else {
            setupKakaoMap()
        }

        dataBinding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // iOS 대응: 주소 복사
        dataBinding.ivStartCopy.setOnClickListener {
            copyToClipboard(args.startAddress)
        }
        dataBinding.ivEndCopy.setOnClickListener {
            copyToClipboard(args.endAddress)
        }

        // iOS 대응: 행별 길찾기 (국내: 카카오내비, 해외: Google Maps)
        dataBinding.tvStartFind.setOnClickListener {
            openNavigation(args.startLat.toDouble(), args.startLng.toDouble(), args.startAddress.orEmpty())
        }
        dataBinding.tvEndFind.setOnClickListener {
            openNavigation(args.endLat.toDouble(), args.endLng.toDouble(), args.endAddress.orEmpty())
        }

        // iOS DeliveryMapViewController 대응: 플레이어 추적 + 플로팅 버튼
        viewModel.initTracking(args.deliveryId, args.statusCd)

        dataBinding.ivTarget.setOnClickListener {
            moveCameraToUserLocation()
        }
        dataBinding.ivRefresh.setOnClickListener {
            viewModel.refreshPlayerGps()
        }
    }

    override fun initObserver() {
        super.initObserver()

        // iOS needsPlayerTracking 대응: refresh 버튼 표시 여부
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.needsPlayerTracking.collectLatest { needs ->
                    dataBinding.ivRefresh.isVisible = needs
                }
            }
        }

        // 플레이어 GPS 마커 갱신 (카카오맵 전용, iOS isDomestic 가드 대응)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playerGps.filterNotNull().collectLatest { gps ->
                    updatePlayerMarker(gps)
                }
            }
        }
    }

    // iOS updatePlayerAnnotation 대응
    private fun updatePlayerMarker(gps: MapDetailViewModel.PlayerGps) {
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) return
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            ensurePlayerStyles(map)
            val position = com.kakao.vectormap.LatLng.from(gps.latitude, gps.longitude)
            playerLabel?.remove()
            playerLabel = layer.addLabel(LabelOptions.from(position).setStyles(playerStyles))

            if (viewModel.fitOnNextGps.value) {
                viewModel.fitOnNextGps.value = false
                fitMapToAllPoints(position)
            }
        }
    }

    // 플레이어 프로필 이미지를 마커 스타일로 로드 (최초 1회 캐시, 실패 시 기본 핀)
    private suspend fun ensurePlayerStyles(map: KakaoMap) {
        if (playerStyles != null) return
        if (!playerBitmapLoaded) {
            playerBitmapLoaded = true
            val url = args.playerProfileImage
            if (!url.isNullOrEmpty()) {
                playerBitmap = withContext(Dispatchers.IO) {
                    runCatching {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(url)
                            .circleCrop()
                            .submit(96, 96)
                            .get()
                    }.getOrNull()
                }
            }
        }
        val bitmap = playerBitmap
        playerStyles = if (bitmap != null) {
            map.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(bitmap)))
        } else {
            map.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.pin_depart)))
        }
    }

    // iOS fitMapToAllPoints 대응: 출발 + 도착 + 플레이어 위치를 모두 포함
    private fun fitMapToAllPoints(playerPosition: com.kakao.vectormap.LatLng) {
        val map = kakaoMap ?: return
        val bb = com.kakao.vectormap.LatLngBounds.Builder()
        bb.include(com.kakao.vectormap.LatLng.from(args.startLat.toDouble(), args.startLng.toDouble()))
        bb.include(com.kakao.vectormap.LatLng.from(args.endLat.toDouble(), args.endLng.toDouble()))
        bb.include(playerPosition)
        map.moveCamera(com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bb.build(), 120))
    }

    // iOS targetButtonTapped 대응: 현재 디바이스 위치로 카메라 이동 (줌 유지)
    @SuppressLint("MissingPermission")
    private fun moveCameraToUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requireContext().toast("위치 권한이 필요합니다.")
            return
        }
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener { location: Location? ->
                location ?: return@addOnSuccessListener
                if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
                    )
                } else {
                    kakaoMap?.moveCamera(
                        com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(
                            com.kakao.vectormap.LatLng.from(location.latitude, location.longitude)
                        )
                    )
                }
            }
    }

    private fun setupKakaoMap() {
        dataBinding.map.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                isMapReady = false
            }

            override fun onMapError(p0: Exception?) {
            }
        }, object : KakaoMapReadyCallback() {
            @SuppressLint("MissingPermission")
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                isMapReady = true

                startStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_depart))
                )
                destStyles = map.labelManager?.addLabelStyles(
                    LabelStyles.from(LabelStyle.from(R.drawable.pin_arrive))
                )

                drawRouteKakaoMapByDirections()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun setupGoogleMap() {
        dataBinding.googleMap.getMapAsync { map ->
            googleMap = map
            map.uiSettings.apply {
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            }
            drawRouteGoogleMap()
        }
    }

    private fun drawRouteKakaoMapByDirections() {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val startLL = com.kakao.vectormap.LatLng.from(args.startLat.toDouble(), args.startLng.toDouble())
            val destLL = com.kakao.vectormap.LatLng.from(args.endLat.toDouble(), args.endLng.toDouble())

            val (points, summary) = runCatching {
                directionsRepo.fetchRoute(
                    startLat = args.startLat.toDouble(),
                    startLng = args.startLng.toDouble(),
                    endLat = args.endLat.toDouble(),
                    endLng = args.endLng.toDouble()
                )
            }.getOrElse {
                Timber.w(it, "Kakao directions failed. Falling back to straight route.")
                emptyList<com.kakao.vectormap.LatLng>() to null
            }
            val routePoints = points.ifEmpty { listOf(startLL, destLL) }

            startLabel?.remove()
            destLabel?.remove()

            startLabel = layer.addLabel(LabelOptions.from(startLL).setStyles(startStyles))
            destLabel = layer.addLabel(LabelOptions.from(destLL).setStyles(destStyles))

            drawRouteOnKakaoMap(
                kakaoMap = map,
                points = routePoints,
                summary = summary,
                clearPrevious = true
            )

            val bb = com.kakao.vectormap.LatLngBounds.Builder()
            routePoints.forEach { bb.include(it) }
            bb.include(startLL)
            bb.include(destLL)

            map.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bb.build(), 120)
            )
        }
    }

    private fun drawRouteGoogleMap() {
        val map = googleMap ?: return
        val start = LatLng(args.startLat.toDouble(), args.startLng.toDouble())
        val end = LatLng(args.endLat.toDouble(), args.endLng.toDouble())

        map.addMarker(MarkerOptions().position(start).title("출발"))
        map.addMarker(MarkerOptions().position(end).title("도착"))

        map.addPolyline(
            PolylineOptions()
                .add(start, end)
                .width(8f)
                .color(ContextCompat.getColor(requireContext(), R.color.primary_100))
        )

        val bounds = LatLngBounds.builder()
            .include(start)
            .include(end)
            .build()

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun copyToClipboard(text: String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("address", text.orEmpty()))
        requireContext().toast("주소가 복사되었습니다.")
    }

    // iOS openNavigation 대응: 국내는 카카오내비, 해외는 Google Maps로 해당 지점 길찾기
    private fun openNavigation(lat: Double, lng: Double, name: String) {
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            openGoogleMaps(lat, lng)
        } else {
            openKakaoNavi(lat, lng, name)
        }
    }

    private fun openKakaoNavi(lat: Double, lng: Double, name: String) {
        val uri = Uri.parse(
            "kakaomap://route?ep=$lat,$lng&by=CAR&eName=${Uri.encode(name)}"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // 카카오맵 미설치 시 플레이스토어로 이동 (iOS App Store 이동 대응)
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map")))
            }.onFailure {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map")))
            }
        }
    }

    private fun openGoogleMaps(lat: Double, lng: Double) {
        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving"
        )
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onStart()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onResume()
        } else {
            dataBinding.map.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onPause()
        } else {
            dataBinding.map.pause()
        }
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onStop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPlayerTracking() // iOS viewWillDisappear 추적 중단 대응
        playerLabel = null
        playerStyles = null
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
            dataBinding.googleMap.onLowMemory()
        }
    }
}
