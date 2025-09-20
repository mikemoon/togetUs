package sky.kr.co.newtogetusa.ui.dialog.message

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogAbroadGuideBinding
import sky.kr.co.newtogetusa.ui.base.BaseDialogFragment

@AndroidEntryPoint
class AbroadGuideDialog : BaseDialogFragment<DialogAbroadGuideBinding, AbroadGuideViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_abroad_guide
    override val viewModel: AbroadGuideViewModel by viewModels()

    var isAgreeCallback: ((isAgree: Boolean) -> Unit)? = null

    override fun init() {
        super.init()
    }

    override fun initObserve() {
        super.initObserve()
        viewModel.event.observe(viewLifecycleOwner){
            when(it){
                is AbroadGuideViewModel.Event.Agree -> {
                    isAgreeCallback?.invoke(true)
                    dismissAllowingStateLoss()
                }
                is AbroadGuideViewModel.Event.Cancel -> {
                    dismissAllowingStateLoss()
                }
            }
        }
    }
}