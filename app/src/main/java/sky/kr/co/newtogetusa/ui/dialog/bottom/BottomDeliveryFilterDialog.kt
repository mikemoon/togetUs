package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.widget.TextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogBottomDeliveryFilterBinding

@AndroidEntryPoint
class BottomDeliveryFilterDialog : BottomBaseDialog<DialogBottomDeliveryFilterBinding, BottomDeliveryFilterViewModel>() {
    override val layoutId: Int
        get() = R.layout.dialog_bottom_delivery_filter
    override val viewModel: BottomDeliveryFilterViewModel by viewModels()

    var initialFilterOption = FilterOption()
    var filterConfirmCallback: ((FilterOption) -> Unit)? = null

    private var myArea: Boolean = true
    private var face2Face: Boolean? = null
    private var immediately: String = IMMEDIATELY_ALL

    override fun init() {
        super.init()
        myArea = initialFilterOption.myArea
        face2Face = initialFilterOption.face2Face
        immediately = initialFilterOption.immediately

        dataBinding.switchMyArea.isChecked = myArea
        dataBinding.switchMyArea.setOnCheckedChangeListener { _, isChecked ->
            myArea = isChecked
        }

        setupFaceToFaceUi()
        setupImmediatelyUi()
        renderFaceToFaceUi()
        renderImmediatelyUi()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                BottomDeliveryFilterViewModel.Event.Back -> dismissAllowingStateLoss()
                BottomDeliveryFilterViewModel.Event.Confirm -> {
                    filterConfirmCallback?.invoke(
                        FilterOption(
                            myArea = myArea,
                            face2Face = face2Face,
                            immediately = immediately
                        )
                    )
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun setupFaceToFaceUi() {
        dataBinding.tvFaceToFaceAll.setOnClickListener {
            face2Face = null
            renderFaceToFaceUi()
        }
        dataBinding.tvFaceToFaceYes.setOnClickListener {
            face2Face = true
            renderFaceToFaceUi()
        }
        dataBinding.tvFaceToFaceNo.setOnClickListener {
            face2Face = false
            renderFaceToFaceUi()
        }
    }

    private fun setupImmediatelyUi() {
        dataBinding.tvPickupAll.setOnClickListener {
            immediately = IMMEDIATELY_ALL
            renderImmediatelyUi()
        }
        dataBinding.tvPickupReserve.setOnClickListener {
            immediately = IMMEDIATELY_RESERVE
            renderImmediatelyUi()
        }
        dataBinding.tvPickupImmediate.setOnClickListener {
            immediately = IMMEDIATELY_IMMEDIATE
            renderImmediatelyUi()
        }
    }

    private fun renderFaceToFaceUi() {
        setSelectedChip(dataBinding.tvFaceToFaceAll, face2Face == null)
        setSelectedChip(dataBinding.tvFaceToFaceYes, face2Face == true)
        setSelectedChip(dataBinding.tvFaceToFaceNo, face2Face == false)
    }

    private fun renderImmediatelyUi() {
        setSelectedChip(dataBinding.tvPickupAll, immediately == IMMEDIATELY_ALL)
        setSelectedChip(dataBinding.tvPickupReserve, immediately == IMMEDIATELY_RESERVE)
        setSelectedChip(dataBinding.tvPickupImmediate, immediately == IMMEDIATELY_IMMEDIATE)
    }

    private fun setSelectedChip(textView: TextView, isSelected: Boolean) {
        val backgroundRes = if (isSelected) {
            R.drawable.background_st_p60_s_p10_r4
        } else {
            R.drawable.background_st_b10_s_w_r4
        }
        val textColorRes = if (isSelected) R.color.primary_100 else R.color.black_80
        textView.setBackgroundResource(backgroundRes)
        textView.setTextColor(requireContext().getColor(textColorRes))
    }

    data class FilterOption(
        val myArea: Boolean = true,
        val face2Face: Boolean? = null,
        val immediately: String = IMMEDIATELY_ALL
    )

    companion object {
        const val IMMEDIATELY_ALL = "ALL"
        const val IMMEDIATELY_RESERVE = "SCHEDULED"
        const val IMMEDIATELY_IMMEDIATE = "IMMEDIATE"
    }
}