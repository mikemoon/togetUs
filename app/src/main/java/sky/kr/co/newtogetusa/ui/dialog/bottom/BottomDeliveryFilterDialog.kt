package sky.kr.co.newtogetusa.ui.dialog.bottom

import android.widget.TextView
import androidx.core.view.isVisible
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
    var showAreaFilter: Boolean = true

    private var myArea: Boolean = true
    private var minFee: Int = 0
    private var face2Face: Boolean? = null
    private var immediately: String? = null

    override fun init() {
        super.init()
        myArea = initialFilterOption.myArea
        minFee = initialFilterOption.minFee
        face2Face = initialFilterOption.face2Face
        immediately = initialFilterOption.immediately

        dataBinding.switchMyArea.isChecked = myArea
        dataBinding.etMinFee.setText(minFee.takeIf { it > 0 }?.toString().orEmpty())
        dataBinding.switchMyArea.setOnCheckedChangeListener { _, isChecked ->
            myArea = isChecked
        }
        setAreaFilterVisible(showAreaFilter)
        dataBinding.tvReset.setOnClickListener {
            myArea = initialFilterOption.myArea
            minFee = 0
            face2Face = null
            immediately = null
            dataBinding.switchMyArea.isChecked = myArea
            dataBinding.etMinFee.setText("")
            renderFaceToFaceUi()
            renderImmediatelyUi()
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
                    minFee = dataBinding.etMinFee.text
                        ?.toString()
                        .orEmpty()
                        .filter { it.isDigit() }
                        .toIntOrNull()
                        ?: 0
                    filterConfirmCallback?.invoke(
                        FilterOption(
                            myArea = myArea,
                            minFee = minFee,
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
            immediately = null
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
        setSelectedChip(dataBinding.tvPickupAll, immediately == null)
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

    private fun setAreaFilterVisible(isVisible: Boolean) {
        dataBinding.clAreaFilterTitle.isVisible = isVisible
        dataBinding.llDepartAreaFilter.isVisible = isVisible
        dataBinding.tvDepartAreaFilter.isVisible = isVisible
        dataBinding.llDestAreaFilter.isVisible = isVisible
        dataBinding.tvDestAreaFilter.isVisible = isVisible
    }

    data class FilterOption(
        val myArea: Boolean = true,
        val minFee: Int = 0,
        val face2Face: Boolean? = null,
        val immediately: String? = null
    )

    companion object {
        const val IMMEDIATELY_RESERVE = "SCHEDULED"
        const val IMMEDIATELY_IMMEDIATE = "IMMEDIATE"
    }
}
