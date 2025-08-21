package sky.kr.co.newtogetusa.ui.dialog.bottom

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomFilterBinding

@AndroidEntryPoint
class BottomFilterDialog : BottomBaseDialog<DialogBottomFilterBinding, BottomFilterViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_filter
    override val viewModel: BottomFilterViewModel by viewModels()

    var itemSelectCallback: ((String) -> Unit)? = null
    var filterList = listOf("거리순","별점순", "거래건순", "최근 거래순")

    override fun init() {
        super.init()
        dataBinding.rv.apply {
            adapter = BottomFilterAdapter(filterList, { selectedItem ->
                itemSelectCallback?.invoke(selectedItem)
                this@BottomFilterDialog.dismissAllowingStateLoss()
            })
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.closeEvent.observe(viewLifecycleOwner){
            dismissAllowingStateLoss()
        }
    }
}