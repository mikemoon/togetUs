package sky.kr.co.newtogetusa.ui.main.home

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.NavGraphDirections
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliverySearchReq
import sky.kr.co.newtogetusa.databinding.FragmentHomeBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeProgressAdapter
import sky.kr.co.newtogetusa.ui.main.home.adapter.HomeRegisteredAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.ApplyAdapter
import sky.kr.co.newtogetusa.ui.main.home.playerAdapter.AvailableAdapter
import sky.kr.co.newtogetusa.utils.dpToPx
import timber.log.Timber
import java.lang.Exception

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

    //구글
    private var googleMap: GoogleMap? = null

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }


    override fun init() {
        super.init()
        arguments?.getBoolean("openDeliveryReq")?.let { open ->
            if (open) {
                arguments?.remove("openDeliveryReq")

                val navController = findNavController()

                // 1️⃣ DeliveryReq로 이동
                navController.navigate(
                    R.id.action_homeTabFragment_to_deliveryReqFragment
                )
                return
            }
        }

        arguments?.getBoolean("openDeliveryFee")?.let { open ->
            if (open) {
                arguments?.remove("openDeliveryFee")

                findNavController().navigate(
                    R.id.action_homeTabFragment_to_deliveryReqFragment
                )
                findNavController().navigate(
                    R.id.action_deliveryReqFragment_to_deliveryFeeFragment
                )
                return
            }
        }



        checkLocationPermission()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            setupGoogleMap()
        }else{
            setupKakaoMap()
        }

        prgAdapter = HomeProgressAdapter{
            selectedItem ->
            val action =
                NavGraphDirections.actionGlobalHistoryDetailFragment(selectedItem)

            requireActivity().findNavController(R.id.nav_host_container).navigate(action)
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

        applyAdapter = ApplyAdapter()
        dataBinding.rvApply.apply {
            adapter = applyAdapter
        }

        availableAdapter = AvailableAdapter()
        dataBinding.rvAvailable.apply {
            adapter = availableAdapter
        }

        if(viewModel.isModePlayer.value){
            viewModel.postPlayerDeliverySearch(DeliverySearchReq(
                type = "DELIVERY|MATCH",
                title = "",
                page_no = 0
            ))
        }else{
            viewModel.postDeliverySearch(DeliverySearchReq(
                type = "DELIVERY|MATCH",
                title = "",
                page_no = 0
            ))
        }
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
                        prgAdapter?.setItems(it)
                    }
                }
                launch {
                    viewModel.applyDeliveryList.filterNotNull().collectLatest {
                        applyAdapter?.setItems(it)
                    }
                }
                launch {
                    viewModel.availableDeliveryList.filterNotNull().collectLatest {
                        availableAdapter?.setItems(it)
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
            }
        }

        dataBinding.ivMyLocation.setOnClickListener {
            if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
                moveCameraToMyLocation()
            }else{
                moveToMyLocationKakao()
            }
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
            googleMap?.isMyLocationEnabled = true
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

        dataBinding.tvIndicator.post {
            val params = dataBinding.tvIndicator.layoutParams
            params.width = dataBinding.tvMap.width
            dataBinding.tvIndicator.layoutParams = params
        }
        dataBinding.tvMap.apply {
            isSelected = true
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvMap)
                setSelectGoogleMapType(true)
            }
        }
        dataBinding.tvSettle.apply {
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvSettle)
                setSelectGoogleMapType(false)
            }
        }
    }

    //구글맵
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectGoogleMapType(isMap: Boolean) {
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
        googleMap?.mapType = if (isMap) GoogleMap.MAP_TYPE_NORMAL else GoogleMap.MAP_TYPE_SATELLITE
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
            //googleMap?.addMarker(MarkerOptions().position(latLng).title("내 위치"))
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
            googleMap?.animateCamera(
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
                googleMap?.animateCamera(
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
                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.kakao_my_loc))) // pin 아이콘

            val layer = kakaoMap!!.labelManager?.layer

            // 2) 마커 추가 (이미 있으면 위치만 갱신)
            val label = layer?.addLabel(LabelOptions.from(here).setStyles(styles))

            if (isFollowMode) {
                kakaoMap?.moveCamera(
                    com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(here),
                    CameraAnimation.from(500, true, true)
                )
                // 팔로우 모드일 때만 트래킹 시작
                kakaoLabel?.let { kakaoMap?.trackingManager?.startTracking(it) }
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

            kakaoMap?.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.newCenterPosition(latLng),
                CameraAnimation.from(500, true, true)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onStart()
        }
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        runCatching {
            if (viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP) {
                dataBinding.googleMap.onResume()
            } else {
                dataBinding.map.resume()
            }
        }.onFailure {  }
        //showStartLocation()
    }

    override fun onPause() {
        super.onPause()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onPause()
        }else {
            dataBinding.map.pause()
        }
        isMapReady = false
    }

    override fun onStop() {
        super.onStop()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onStop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if(viewModel.mapShowState.value == HomeTabViewModel.MapShow.GOOGLE_MAP){
            dataBinding.googleMap.onLowMemory()
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
}