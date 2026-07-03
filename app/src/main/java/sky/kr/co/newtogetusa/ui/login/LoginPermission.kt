package sky.kr.co.newtogetusa.ui.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentLoginPermissionBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment

@AndroidEntryPoint
class LoginPermission : BaseFragment<FragmentLoginPermissionBinding, LoginViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_login_permission
    override val viewModel: LoginViewModel by viewModels()

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            completePermissionGuide()
        }

    override fun init() {
        super.init()
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.tvConfirm.setOnClickListener {
            requestAllPermissions()
        }
    }

    private fun requestAllPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.CAMERA)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isEmpty()) {
            completePermissionGuide()
        } else {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun completePermissionGuide() {
        viewModel.markPermissionGuideShown {
            moveToNextScreen()
        }
    }

    private fun moveToNextScreen() {
        findNavController().navigate(R.id.action_loginPermission_to_loginFragment)
    }
}
