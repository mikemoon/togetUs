package sky.kr.co.newtogetusa.ui.main.global

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentTakePhotoBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.message.MessageDialog
import timber.log.Timber
import java.io.File
import java.util.Locale

@AndroidEntryPoint
class TakePhotoFragment : BaseFragment<FragmentTakePhotoBinding, TakePhotoVM>() {
    override val layoutId: Int
        get() = R.layout.fragment_take_photo
    override val viewModel: TakePhotoVM by viewModels()
    private val args: TakePhotoFragmentArgs by navArgs()

    private var imageCapture: ImageCapture? = null
    private var isCameraReady = false
    private var isCameraStarting = false
    private var pendingLocationAction: ((Location) -> Unit)? = null

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val cameraGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (cameraGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

    private val requestLocationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                requestCurrentLocationForAction()
            } else {
                pendingLocationAction = null
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun init() {
        viewModel.loadDeliveryInfo(args.deliveryId, args.proofType)
        bindClicks()
        checkAndStartCamera()
    }

    override fun onResume() {
        super.onResume()
        // 설정 화면에서 카메라 권한을 허용하고 돌아온 경우 다시 카메라를 연결한다.
        if (!isCameraReady && hasCameraPermission()) {
            startCamera()
        }
    }

    private fun bindClicks() {
        dataBinding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        dataBinding.btnTakePhoto.setOnClickListener {
            capturePhoto()
        }

        dataBinding.btnRetake.setOnClickListener {
            viewModel.setRetakeMode(false)
            dataBinding.ivCapturedPhoto.setImageDrawable(null)
            if (!isCameraReady && hasCameraPermission()) {
                startCamera()
            }
        }

        dataBinding.btnUpload.setOnClickListener {
            runAfterLocationCheck { location ->
                viewModel.updateCapturedLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = null
                )
                viewModel.uploadCapturedPhoto(args.deliveryId, args.proofType)
            }
        }

        dataBinding.tvUnableTakePhoto.setOnClickListener {
            runAfterLocationCheck {
                findNavController().navigate(
                    TakePhotoFragmentDirections.actionGlobalNoPictureFragment(args.deliveryId, args.proofType)
                )
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                TakePhotoVM.Event.UploadSuccess -> {
                    Toast.makeText(requireContext(), "촬영 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                TakePhotoVM.Event.UploadFailed -> Toast.makeText(requireContext(), "촬영 정보 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                TakePhotoVM.Event.Invalid -> Toast.makeText(requireContext(), "촬영 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndStartCamera() {
        // 위치 정보는 촬영 후 EXIF/API에 선택적으로 첨부한다. 위치 권한 때문에
        // 카메라 자체가 비활성화되면 안 된다.
        if (hasCameraPermission()) {
            startCamera()
            return
        }

        requestPermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun startCamera() {
        if (!hasCameraPermission() || isCameraReady || isCameraStarting) return

        isCameraStarting = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(dataBinding.previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture
                )
                imageCapture = capture
                isCameraReady = true
                dataBinding.btnTakePhoto.isEnabled = true
            } catch (e: Exception) {
                imageCapture = null
                isCameraReady = false
                Timber.e(e, "Use case binding failed")
                Toast.makeText(requireContext(), "카메라 시작에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                isCameraStarting = false
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture
        if (!isCameraReady || imageCapture == null) {
            Toast.makeText(requireContext(), "카메라를 준비 중입니다.", Toast.LENGTH_SHORT).show()
            if (hasCameraPermission()) startCamera()
            return
        }

        val photoFile = File(
            requireContext().cacheDir,
            "capture_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    dataBinding.ivCapturedPhoto.setImageURI(Uri.fromFile(photoFile))
                    viewModel.onPhotoCaptured(photoFile, null, null, null)
                    attachLocationAndAddress(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.e(exception, "Photo capture failed")
                    Toast.makeText(requireContext(), "촬영에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun attachLocationAndAddress(photoFile: File) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            viewModel.onPhotoCaptured(photoFile, null, null, null)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { fallbackLocation ->
                            writeMetaAndUpdateState(photoFile, fallbackLocation)
                        }
                        .addOnFailureListener {
                            writeMetaAndUpdateState(photoFile, null)
                        }
                } else {
                    writeMetaAndUpdateState(photoFile, location)
                }
            }
            .addOnFailureListener {
                writeMetaAndUpdateState(photoFile, null)
            }
    }

    private fun writeMetaAndUpdateState(photoFile: File, location: Location?) {
        resolveAddress(location) { address ->
            writeExifMeta(photoFile, location, address)
            if (!isAdded) return@resolveAddress
            requireActivity().runOnUiThread {
                viewModel.onPhotoCaptured(
                    file = photoFile,
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    address = address
                )
            }
        }
    }

    private fun resolveAddress(location: Location?, callback: (String?) -> Unit) {
        if (location == null || !Geocoder.isPresent()) {
            callback(null)
            return
        }

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                    callback(addresses.firstOrNull()?.getAddressLine(0))
                }

                override fun onError(errorMessage: String?) {
                    callback(null)
                }
            })
            return
        }

        // API 33 미만 동기 API는 네트워크/디스크 I/O 블로킹 가능 → 백그라운드에서 호출
        Thread {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }.onSuccess { addresses ->
                callback(addresses?.firstOrNull()?.getAddressLine(0))
            }.onFailure {
                callback(null)
            }
        }.start()
    }

    private fun writeExifMeta(photoFile: File, location: Location?, address: String?) {
        runCatching {
            val exif = ExifInterface(photoFile)
            if (location != null) {
                exif.setGpsInfo(location)
            }
            if (!address.isNullOrBlank()) {
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, address)
            }
            exif.saveAttributes()
        }.onFailure {
            Timber.w(it, "Failed to write EXIF metadata")
        }
    }

    private fun runAfterLocationCheck(action: (Location) -> Unit) {
        pendingLocationAction = action
        if (!hasLocationPermission()) {
            requestLocationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        requestCurrentLocationForAction()
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocationForAction() {
        if (!hasLocationPermission()) return

        viewModel.loadingState.value = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    handleLocationForAction(location)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { fallback ->
                            if (fallback != null) {
                                handleLocationForAction(fallback)
                            } else {
                                handleLocationUnavailable()
                            }
                        }
                        .addOnFailureListener {
                            handleLocationUnavailable()
                        }
                }
            }
            .addOnFailureListener {
                handleLocationUnavailable()
            }
    }

    private fun handleLocationForAction(location: Location) {
        viewModel.loadingState.value = false
        val action = pendingLocationAction ?: return
        val target = viewModel.targetLocation.value
        if (target == null || isWithinRange(location, target)) {
            pendingLocationAction = null
            action(location)
            return
        }

        val targetName = if (args.proofType == TakePhotoVM.PROOF_PICKUP) "픽업지" else "도착지"
        MessageDialog.newInstance(
            msg = "현재 위치가 ${targetName}와 다릅니다(GPS 오류 가능성). 실제 ${targetName}가 아니라면 추후 증빙에 불이익이 발생할 수 있습니다.",
            msgTitle = "현재 위치를 확인해주세요",
            leftBtn = "취소",
            rightBtn = "계속 진행"
        ).onLeftBtn {
            pendingLocationAction = null
        }.onRightBtn {
            pendingLocationAction = null
            action(location)
        }.show(childFragmentManager, "proof_location_check")
    }

    private fun handleLocationUnavailable() {
        viewModel.loadingState.value = false
        pendingLocationAction = null
        Toast.makeText(requireContext(), "현재 위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun isWithinRange(
        current: Location,
        target: TakePhotoVM.TargetLocation
    ): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(
            current.latitude,
            current.longitude,
            target.latitude,
            target.longitude,
            result
        )
        return result.firstOrNull()?.let { it <= LOCATION_CHECK_RANGE_METERS } ?: false
    }

    companion object {
        private const val LOCATION_CHECK_RANGE_METERS = 500f
    }
}
