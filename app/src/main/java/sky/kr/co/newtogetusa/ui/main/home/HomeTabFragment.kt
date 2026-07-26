package sky.kr.co.newtogetusa.ui.main.home

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
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
import com.kakao.vectormap.MapType
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.NavGraphDirections
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentHomeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomLocationAlarmDialog
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeBannerAdapter
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeRegisteredAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.ApplyAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.AvailableAdapter
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.hideLoading
import sky.kr.co.newtogetusa.utils.loadImage
import sky.kr.co.newtogetusa.utils.showLoading
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.lang.Exception
import kotlin.math.abs

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeBinding, HomeTabViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_home
    override val viewModel: HomeTabViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var prgAdapter: HomeProgressAdapter? = null
    private var regAdapter: HomeRegisteredAdapter? = null
    private var applyAdapter : ApplyAdapter? = null
    private var availableAdapter : AvailableAdapter? = null

    //카카오
    private var isMapReady = false
    private var kakaoMap : KakaoMap? = null
    private var kakaoLabel: com.kakao.vectormap.label.Label? = null
    private var kakaoLabelStyles: com.kakao.vectormap.label.LabelStyles? = null
    private var lastKnownLocation: Location? = null
    private var isFollowMode: Boolean = true
    private var isNormalMapType: Boolean = true

    //구글
    private var googleMap: GoogleMap? = null
    private var googleCurrentLocationMarker: Marker? = null

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private var bannerAdapter: HomeBannerAdapter? = null
    private var bannerSwitchHandler: Handler? = null
    private var bannerSwitchRunnable: Runnable? = null
    private var currentBannerIndex = 0
    private var isBannerDragging = false
    private var bannerPageChangeCallback: ViewPager2.OnPageChangeCallback? = null


    override fun init() {
        super.init()
        checkLocationPermission()
        setupGoogleMap()
        setupMapTypeToggle()

        prgAdapter = HomeProgressAdapter { selectedItem ->
            if (viewModel.isModePlayer.value) {
                requireActivity().findNavController(R.id.nav_host_container).navigate(
                    Uri.parse("togetus://player-history-detail/${selectedItem.delivery_id}")
                )
            } else {
                val action = NavGraphDirections.actionGlobalHistoryDetailFragment(selectedItem)
                requireActivity().findNavController(R.id.nav_host_container).navigate(action)
            }
        }
        dataBinding.rvProgress.apply {
            adapter = prgAdapter
        }
        regAdapter = HomeRegisteredAdapter{ selectedItem ->
            val action =
                NavGraphDirections.actionGlobalHistoryDetailFragment(selectedItem)
            requireActivity().findNavController(R.id.nav_host_container).navigate(action)
        }
        dataBinding.rvRegistered.apply {
            adapter = regAdapter
        }

        applyAdapter = ApplyAdapter { selectedItem ->
            requireActivity().findNavController(R.id.nav_host_container).navigate(
                Uri.parse("togetus://player-history-detail/${selectedItem.delivery_id}")
            )
        }
        dataBinding.rvApply.apply {
            adapter = applyAdapter
        }

        availableAdapter = AvailableAdapter{ selectedItem ->
            requireActivity().findNavController(R.id.nav_host_container).navigate(
                Uri.parse("togetus://player-history-detail/${selectedItem.delivery_id}")
            )
        }
        dataBinding.rvAvailable.apply {
            adapter = availableAdapter
        }

        bannerAdapter = HomeBannerAdapter { banner ->
            viewModel.getBannerDetail(banner.bannerId)
        }
        dataBinding.vpBanner.adapter = bannerAdapter

        consumeDeliveryOpenArguments()
    }

    @SuppressLint("MissingPermission")
    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.isModePlayer.collectLatest {

                    }
                }
                launch {
                    viewModel.doingDeliveryList.filterNotNull().collectLatest {
                        Timber.d("hometab doingDelivery: ${it.size}")
                        prgAdapter?.setItems(it)
                    }
                }
                launch {
                    viewModel.registeredDeliveryList.filterNotNull().collectLatest {
                        Timber.d("hometab regDelivery: ${it.size}")
                        regAdapter?.setItems(it)
                    }
                }
                launch {
                    viewModel.doingPlayerDeliveryList.filterNotNull().collectLatest {
                        prgAdapter?.setItems(it, viewModel.doingPlayerDeliveryHasMore.value)
                    }
                }
                launch {
                    viewModel.applyDeliveryList.filterNotNull().collectLatest {
                        applyAdapter?.setItems(it, viewModel.applyDeliveryHasMore.value)
                    }
                }
                launch {
                    viewModel.availableDeliveryList.filterNotNull().collectLatest {
                        availableAdapter?.setItems(it)
                    }
                }
                launch {
                    viewModel.locationAlarmOn.collectLatest { isOn ->
                        updateLocationAlarmBadge(isOn)
                    }
                }
                launch {
                    viewModel.loadingState.collectLatest { isLoading ->
                        if (isLoading) showLoading() else hideLoading()
                    }
                }

                launch {
                    viewModel.bannerList.filterNotNull().collectLatest { banners ->
                        Timber.d("hometab bannerList: ${banners}")
                        bannerAdapter?.setItems(banners)
                        currentBannerIndex = 0
                        dataBinding.vpBanner.setCurrentItem(0, false)
                        setupBannerIndicator(banners.size)
                        startBannerAutoSwitch()
                    }
                }

                launch {
                    viewModel.bannerDetail.filterNotNull().collectLatest { landing ->
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(landing.landingUrl))
                            startActivity(intent)
                        }.onFailure {
                        }
                        viewModel.clearBannerDetail()
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                HomeTabViewModel.Event.JoinPlayer ->{
                    //findNavController().navigate(R.id.action_homeTabFragment_to_playerJoinFragment)
                }
                HomeTabViewModel.Event.RequestDelivery ->{
                    findNavController().navigate(R.id.action_homeTabFragment_to_deliveryReqFragment)
                }
                HomeTabViewModel.Event.Alarm -> {
                    findNavController().navigate(R.id.action_homeTabFragment_to_homeNotificationFragment)
                }
                is HomeTabViewModel.Event.LocationAlarmChanged -> {
                    hideLoading()
                    if (it.isOn) refreshLocationAlarmPosition()
                    showLocationAlarmPopup(it.isOn)
                }
                HomeTabViewModel.Event.LocationAlarmFailed -> {
                    hideLoading()
                    requireContext().toast("현위치 동행 알림 설정에 실패했습니다.")
                }
            }
        }

        dataBinding.ivMyLocation.setOnClickListener {
            moveCameraToMyLocation()
        }
        dataBinding.llLocationAlarm.setOnClickListener {
            viewModel.toggleLocationAlarm()
        }

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
            googleMap?.uiSettings?.apply {
                isZoomControlsEnabled = false
                isMyLocationButtonEnabled = false
            }
            enableMyLocation()
            map.setOnMapClickListener { latLng ->
                //onMapTapped(latLng)
            }
            maybeInitMapWithLocation()
        }
    }

    private fun setupMapTypeToggle() {
        dataBinding.tvIndicator.post {
            val params = dataBinding.tvIndicator.layoutParams
            params.width = dataBinding.tvMap.width
            dataBinding.tvIndicator.layoutParams = params
        }
        dataBinding.tvMap.apply {
            isSelected = true
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvMap)
                setSelectMapType(true)
            }
        }
        dataBinding.tvSettle.apply {
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvSettle)
                setSelectMapType(false)
            }
        }
        setSelectMapType(isNormalMapType)
    }

    private fun updateLocationAlarmBadge(isOn: Boolean) {
        val bgRes = if (isOn) {
            R.drawable.background_location_alarm_on
        } else {
            R.drawable.background_location_alarm_off
        }
        val colorRes = if (isOn) {
            R.color.primary_100
        } else {
            R.color.location_alarm_off_text
        }
        val color = ContextCompat.getColor(requireContext(), colorRes)
        dataBinding.llLocationAlarm.background = ContextCompat.getDrawable(requireContext(), bgRes)
        // iOS 대응: 내 주변 동행 ON/OFF 문구 표시
        dataBinding.tvLocationAlarm.text = if (isOn) "내 주변 동행 ON" else "내 주변 동행 OFF"
        dataBinding.tvLocationAlarm.setTextColor(color)
        dataBinding.ivLocationAlarm.setColorFilter(color)
    }

    private fun showLocationAlarmPopup(isOn: Boolean) {
        BottomLocationAlarmDialog().apply {
            this.isOn = isOn
            onSettingClick = {
                // iOS 대응: 동행 예약 선택 시 프로필 화면으로 이동
                findNavController().navigate(
                    R.id.action_homeTabFragment_to_profileManagementFragment,
                    bundleOf("isPlayer" to true)
                )
            }
        }.show(parentFragmentManager, "LocationAlarmDialog")
    }

    @SuppressLint("MissingPermission")
    private fun refreshLocationAlarmPosition() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        lastKnownLocation?.let {
            viewModel.refreshLocationAlarm(it.latitude, it.longitude)
            return
        }

        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener
            lastKnownLocation = location
            viewModel.refreshLocationAlarm(location.latitude, location.longitude)
        }
    }

    //구글맵/카카오맵
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectMapType(isMap: Boolean) {
        isNormalMapType = isMap
        dataBinding.tvMap.apply {
            isSelected = isMap
            //background = if(isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (isMap) R.color.white else R.color.black_80))
        }
        dataBinding.tvSettle.apply {
            isSelected = !isMap
            //background = if(!isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (!isMap) R.color.white else R.color.black_80))
        }
        when (viewModel.mapShowState.value) {
            HomeTabViewModel.MapShow.GOOGLE_MAP -> {
                googleMap?.mapType = if (isMap) GoogleMap.MAP_TYPE_NORMAL else GoogleMap.MAP_TYPE_SATELLITE
            }
            HomeTabViewModel.MapShow.KAKAO_MAP -> {
                kakaoMap?.changeMapType(if (isMap) MapType.NORMAL else MapType.SKYVIEW)
            }
            HomeTabViewModel.MapShow.LOCAL_IMAGE -> Unit
        }
    }

    //구글맵
    private fun moveIndicatorTo(target: View) {
        val animator =
            ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
    }

    //구글맵
    private fun maybeInitMapWithLocation() {
        if (googleMap != null && lastKnownLocation != null) {
            val latLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
            showGoogleCurrentLocation(lastKnownLocation!!)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        if (lastKnownLocation == null) {//서울
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.5665, 126.9780), 12f))
        }
    }

    //구글맵
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private fun moveCameraToMyLocation() {
        //17f 도보, 15f 지역 정도
        // 1) 이미 위치를 알고 있으면 바로 이동
        lastKnownLocation?.let { loc ->
            val latLng = LatLng(loc.latitude, loc.longitude)
            showGoogleCurrentLocation(loc)
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            )
            return
        }

        // 2) 없으면 현재 위치 요청
        val cts = CancellationTokenSource()
        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { loc ->
            if (loc != null) {
                lastKnownLocation = loc
                val latLng = LatLng(loc.latitude, loc.longitude)
                showGoogleCurrentLocation(loc)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                )
            }
        }
    }



    private fun setupKakaoMap(){
        dataBinding.map.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    //if (isFollowMode) disableFollowMode()
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
                setSelectMapType(isNormalMapType)

                kakaoLabelStyles = kakaoMap!!.labelManager?.addLabelStyles(
                    com.kakao.vectormap.label.LabelStyles.from(
                        com.kakao.vectormap.label.LabelStyle.from(R.drawable.kakao_my_loc)
                    )
                )
                showMyLocation()

                //applyPendingIfAny()
            }
        })
    }

    //카카오맵
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

    //카카오맵
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
            Timber.e("KakaoMap getCurrentLocation failed $e")
        }
    }

    //카카오맵
    private fun showStartLocation(){
        lastKnownLocation?.let { loc ->
            val here = com.kakao.vectormap.LatLng.from(loc.latitude, loc.longitude)
            viewModel.fetchAddress(loc.latitude, loc.longitude)
            // 1) 마커(Label) 스타일/레이어
            val styles = kakaoMap!!.labelManager
                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.kakao_my_loc)))

            val layer = kakaoMap!!.labelManager?.layer

            // 2) 마커 추가 (이미 있으면 위치만 갱신)
            val label = layer?.addLabel(LabelOptions.from(here).setStyles(styles))

            if (isFollowMode) {
                kakaoMap?.moveCamera(
                    com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here)
                )
                kakaoMap?.trackingManager?.stopTracking()
            } else {
                // 팔로우 꺼져 있으면 혹시 모를 트래킹 종료
                kakaoMap?.trackingManager?.stopTracking()
            }
            // kakaoMap!!.trackingManager.setTrackingRotation(false) // 회전 동기화 여부
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private fun moveToMyLocationKakao() {
        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener

            val latLng = com.kakao.vectormap.LatLng.from(
                location.latitude,
                location.longitude
            )
            showKakaoCurrentLocation(latLng)

            kakaoMap?.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(latLng)
            )
        }
    }

    private fun showGoogleCurrentLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("내 위치")
            .anchor(0.5f, 1f)

        googleCurrentLocationMarker?.remove()
        googleCurrentLocationMarker = googleMap?.addMarker(markerOptions)
    }

    private fun showKakaoCurrentLocation(latLng: com.kakao.vectormap.LatLng) {
        val layer = kakaoMap?.labelManager?.layer ?: return
        kakaoLabel?.remove()
        kakaoLabel = layer.addLabel(LabelOptions.from(latLng).setStyles(kakaoLabelStyles))
    }

    private fun setupBannerPager() {
        bannerPageChangeCallback?.let { dataBinding.vpBanner.unregisterOnPageChangeCallback(it) }
        bannerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentBannerIndex = position
                setSelectedIndicator(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                isBannerDragging = state == ViewPager2.SCROLL_STATE_DRAGGING
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopBannerAutoSwitch()
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startBannerAutoSwitch()
                }
            }
        }
        bannerPageChangeCallback?.let { dataBinding.vpBanner.registerOnPageChangeCallback(it) }
    }

    private fun setupBannerIndicator(count: Int) {
        dataBinding.llBannerIndicator.removeAllViews()
        if (count <= 1) {
            dataBinding.llBannerIndicator.visibility = View.GONE
            return
        }

        dataBinding.llBannerIndicator.visibility = View.VISIBLE

        repeat(count) { idx ->
            val dot = ImageView(requireContext()).apply {
                setImageResource(R.drawable.shape_ellipse_indicator)
                layoutParams = LinearLayout.LayoutParams(6.dpToPx(), 6.dpToPx()).apply {
                    marginStart = if (idx == 0) 0 else 4.dpToPx()
                }
            }
            dataBinding.llBannerIndicator.addView(dot)
        }
        setSelectedIndicator(currentBannerIndex)
    }

    private fun setSelectedIndicator(selectedIndex: Int) {
        val childCount = dataBinding.llBannerIndicator.childCount
        for (i in 0 until childCount) {
            val child = dataBinding.llBannerIndicator.getChildAt(i)
            child.alpha = if (i == selectedIndex) 1f else 0.4f
        }
    }

    private fun startBannerAutoSwitch() {
        stopBannerAutoSwitch()
        val banners = viewModel.bannerList.value ?: return
        if (banners.size <= 1 || isBannerDragging) return

        bannerSwitchHandler = Handler(Looper.getMainLooper())
        bannerSwitchRunnable = object : Runnable {
            override fun run() {
                val next = (currentBannerIndex + 1) % banners.size
                dataBinding.vpBanner.setCurrentItem(next, true)
                bannerSwitchHandler?.postDelayed(this, 3000)
            }
        }.also {
            bannerSwitchHandler?.postDelayed(it, 3000)
        }
    }

    private fun stopBannerAutoSwitch() {
        bannerSwitchRunnable?.let { runnable ->
            bannerSwitchHandler?.removeCallbacks(runnable)
        }
        bannerSwitchRunnable = null
        bannerSwitchHandler = null
    }

    private fun consumeDeliveryOpenArguments() {
        val args = arguments ?: return
        if (args.getBoolean("openDeliveryReq", false)) {
            val returnToHistory = args.getBoolean("returnToHistory", false)
            val returnToDetail = args.getBoolean("returnToDetail", false)
            val isEdit = args.getBoolean("isEdit", false)
            val deliveryId = args.getLong("deliveryId", -1L)
            args.remove("openDeliveryReq")
            args.remove("returnToHistory")
            args.remove("returnToDetail")
            args.remove("isEdit")
            args.remove("deliveryId")

            dataBinding.root.post {
                val navController = findNavController()
                if (navController.currentDestination?.id != R.id.homeTabFragment) return@post
                navController.navigate(
                    R.id.action_homeTabFragment_to_deliveryReqFragment,
                    Bundle().apply {
                        putBoolean("returnToHistory", returnToHistory)
                        putBoolean("returnToDetail", returnToDetail)
                        putBoolean("isEdit", isEdit)
                        putLong("deliveryId", deliveryId)
                    }
                )
            }
        }

        if (args.getBoolean("openDeliveryFee", false)) {
            args.remove("openDeliveryFee")
            args.remove("returnToHistory")
            args.remove("returnToDetail")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBannerPager()

        dataBinding.googleMap.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dataBinding.googleMap.onStart()
        startBannerAutoSwitch()
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        consumeDeliveryOpenArguments()
        viewModel.refreshHome()
        runCatching { dataBinding.googleMap.onResume() }.onFailure {  }
        //showStartLocation()
    }

    override fun onPause() {
        super.onPause()
        dataBinding.googleMap.onPause()
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        stopBannerAutoSwitch()
        dataBinding.googleMap.onStop()
    }

    override fun onDestroyView() {
        stopBannerAutoSwitch()
        bannerPageChangeCallback?.let { dataBinding.vpBanner.unregisterOnPageChangeCallback(it) }
        bannerPageChangeCallback = null
        super.onDestroyView()
        dataBinding.googleMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        dataBinding.googleMap.onLowMemory()
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
}
