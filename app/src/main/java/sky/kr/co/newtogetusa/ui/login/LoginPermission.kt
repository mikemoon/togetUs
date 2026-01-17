package sky.kr.co.newtogetusa.ui.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 승인되었을 때 처리할 로직 (예: 다음 화면으로 이동)
                //moveToNextScreen()
            } else {
                // 권한이 거부되었을 때 처리할 로직 (예: 사용자에게 설명 또는 기능 제한)
                // 이 예제에서는 권한이 필수는 아니므로 그냥 다음 화면으로 이동합니다.
                //moveToNextScreen()
            }
        }

    override fun init() {
        super.init()
        checkAndRequestNotificationPermission()
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.tvConfirm.setOnClickListener {
            moveToNextScreen()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        // Android 13 (API 33) 이상 버전에서만 권한이 필요합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // 1. 이미 권한이 승인된 경우
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 바로 다음 화면으로 이동
                    //moveToNextScreen()
                }
                // 2. 사용자가 명시적으로 권한을 거부한 적이 있는지 확인 (선택 사항)
                // 이전에 거부한 사용자에게는 권한이 왜 필요한지 설명하는 UI를 보여주는 것이 좋습니다.
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 설명 후 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                // 3. 권한이 없는 다른 모든 경우 (처음 보거나, 다시 묻지 않음 선택)
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 13 미만 버전에서는 권한이 필요 없으므로 바로 다음 단계 진행
            //moveToNextScreen()
        }
    }

    private fun moveToNextScreen() {
        findNavController().navigate(R.id.action_loginPermission_to_loginFragment)
    }
}