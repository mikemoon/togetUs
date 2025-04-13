package sky.kr.co.newtogetusa.ui.main.delivery

import android.graphics.Color
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryProductBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.utils.dpToPx

@AndroidEntryPoint
class DeliveryProductFragment : BaseFragment<FragmentDeliveryProductBinding, DeliveryProductViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_product
    override val viewModel: DeliveryProductViewModel by viewModels()

    override fun init() {
        super.init()

        val categories = listOf("서류/문서", "전자기기", "음식/식품", "생활/잡화", "귀중품", "기타/다중")
        categories.forEach { category ->

            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = category
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductSort.addView(itemTv)

        }

        val weights = listOf("가벼움 (~3KG)", "보통 (3~10KG)", "무거움 (10KG~)")
        weights.forEach { weight ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = weight
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductWeight.addView(itemTv)
        }

        val sizes = listOf("작음 (작은 상자/에코백 수준)", "중간 (두 손으로 안을 수준)", "큼 (대형 박스/차량 필요)")
        sizes.forEach { size ->
            val itemTv = AppCompatTextView(requireContext(), null).apply {
                text = size
                setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(context, R.drawable.background_s_b5_r4)
                setTextColor(requireContext().getColor(R.color.black_80))
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8.dpToPx(), 8.dpToPx(), 0)
                }

                setOnClickListener{
                    isSelected = !isSelected
                    background = ContextCompat.getDrawable(context, if(isSelected)R.drawable.background_st_p60_s_p10_r4 else R.drawable.background_s_b5_r4)
                    setTextColor(requireContext().getColor(if(isSelected)R.color.primary_100 else R.color.black_80))
                }
            }
            dataBinding.flProductSize.addView(itemTv)
        }
    }
}