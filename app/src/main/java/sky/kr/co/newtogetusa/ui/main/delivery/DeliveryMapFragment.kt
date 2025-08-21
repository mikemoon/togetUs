package sky.kr.co.newtogetusa.ui.main.delivery

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryMapBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class DeliveryMapFragment : BaseFragment<FragmentDeliveryMapBinding, DeliveryMapViewModel>()  {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_map
    override val viewModel: DeliveryMapViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var googleMap: GoogleMap? = null
    private var lastKnownLocation: Location? = null

    private var startAddress: SearchResultModel? = null
    private var endAddress: SearchResultModel? = null

    private var startLatLng: LatLng? = null
    private var endLatLng: LatLng? = null

    override fun onCreateView(savedInstanceState: Bundle?) {
        super.onCreateView(savedInstanceState)

    }

    override fun init() {
        super.init()
        checkLocationPermission()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    lastKnownLocation = location
                    maybeInitMapWithLocation()
                }
            }

        dataBinding.map.start(object : MapLifeCycleCallback(){
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
            }

        }, object : KakaoMapReadyCallback(){
            override fun onMapReady(p0: KakaoMap) {
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

    override fun onStart() {
        super.onStart()
        //dataBinding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        dataBinding.map.resume()
    }

    override fun onPause() {
        super.onPause()
        dataBinding.map.pause()
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

    override fun initObserver() {
        super.initObserver()

        parentFragmentManager.setFragmentResultListener("fromB", viewLifecycleOwner) { requestKey, bundle ->
            val result = bundle.getParcelable<SearchResultModel>("selectedValue") // key에 맞게 꺼냄
            Timber.d("FragmentA 받은 값: $result")
            if(startLatLng == null){
                startAddress = result
                viewModel.fetchLatLng(result!!.placeId!!){
                    startLatLng = it
                }
            }
            else if(endLatLng == null){
                endAddress = result
                viewModel.fetchLatLng(result!!.placeId!!){
                    endLatLng = it
                    if(startLatLng != null && endLatLng != null){
                        viewModel.fetchRoute(startLatLng!!, endLatLng!!)
                    }
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
                    findNavController().navigate(R.id.action_deliveryMapFragment_to_deliveryStartFragment2)
                }
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
}