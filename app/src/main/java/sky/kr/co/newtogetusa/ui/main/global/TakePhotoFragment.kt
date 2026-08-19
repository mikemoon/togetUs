package sky.kr.co.newtogetusa.ui.main.global

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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

    override fun init() {
        bindClicks()
        checkAndStartCamera()
    }

    private fun bindClicks() {
        dataBinding.ivClose.setOnClickListener {
            findNavController().popBackStack()
        }

        dataBinding.btnTakePhoto.setOnClickListener {
            capturePhoto()
        }

        dataBinding.btnRetake.setOnClickListener {
            capturePhoto()
        }

        dataBinding.btnUpload.setOnClickListener {
            viewModel.uploadCapturedPhoto(args.deliveryId, args.proofType)
        }

        dataBinding.tvUnableTakePhoto.setOnClickListener {
            findNavController().navigate(
                TakePhotoFragmentDirections.actionGlobalNoPictureFragment(args.deliveryId, args.proofType)
            )
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
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val hasAllPermissions = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }

        if (hasAllPermissions) {
            startCamera()
            return
        }

        val cameraGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (cameraGranted) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            startCamera()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(dataBinding.previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Timber.e(e, "Use case binding failed")
                Toast.makeText(requireContext(), "카메라 시작에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

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
            viewModel.onPhotoCaptured(
                file = photoFile,
                latitude = location?.latitude,
                longitude = location?.longitude,
                address = address
            )
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
}
