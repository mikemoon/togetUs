package sky.kr.co.newtogetusa.ui.main.delivery

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPickupBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomCalendarDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomTimeDialog
import sky.kr.co.newtogetusa.utils.dialogFragmentShow
import sky.kr.co.newtogetusa.utils.dpToPx
import sky.kr.co.newtogetusa.utils.toKoreanDateYYYYMMDDEEEE
import sky.kr.co.newtogetusa.utils.toYYYYMMDD
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class DeliveryPickupFragment :
    BaseFragment<FragmentDeliveryPickupBinding, DeliveryPickupViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pickup
    override val viewModel: DeliveryPickupViewModel by viewModels()

    private val sharedViewModel : DeliveryRequestSharedViewModel by navGraphViewModels(R.id.nav_graph)

    override fun init() {
        super.init()

        dataBinding.tvIndicator.post {
            val state = sharedViewModel.state.value
            val hasPickupState = state.pickupDate != null || state.pickupTime != null
            val isImmediately = if (hasPickupState) state.pickupIsImmediately else true
            val target = if (isImmediately) dataBinding.tvImmediate else dataBinding.tvReservation
            val params = dataBinding.tvIndicator.layoutParams
            params.width = target.width
            dataBinding.tvIndicator.layoutParams = params
            moveIndicatorTo(target)
            setSelectPickupType(isReservation = !isImmediately, clearDateTime = false)
            restorePickupFields(state)
        }
        dataBinding.tvReservation.apply {
            //setTextColor(requireContext().getColor(R.color.white))
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvReservation)
                setSelectPickupType(true)
            }
        }
        dataBinding.tvImmediate.apply {
            isSelected = true
            setOnClickListener {
                moveIndicatorTo(dataBinding.tvImmediate)
                setSelectPickupType(false)
            }
        }

        dataBinding.tvIndicator2.post {
            val params = dataBinding.tvIndicator2.layoutParams
            params.width = dataBinding.tvMeet.width
            dataBinding.tvIndicator2.layoutParams = params
        }
        dataBinding.tvMeet.apply {
            isSelected = true
            setOnClickListener {
                moveIndicator2To(dataBinding.tvMeet)
                setSelectMeetType(true)
            }
        }
        dataBinding.tvNotMeet.apply {
            setOnClickListener {
                moveIndicator2To(dataBinding.tvNotMeet)
                setSelectMeetType(false)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectPickupType(isReservation: Boolean, clearDateTime: Boolean = true) {
        dataBinding.tvReservation.apply {
            isSelected = isReservation
            //background = if(isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (isReservation) R.color.white else R.color.black_80))
        }
        dataBinding.tvImmediate.apply {
            isSelected = !isReservation
            //background = if(!isReservation)requireContext().getDrawable(R.drawable.background_s_b80_r24) else null
            setTextColor(requireContext().getColor(if (!isReservation) R.color.white else R.color.black_80))
        }
        viewModel.isImmediately.value = !isReservation
        setDateTimeSelectEnabled(isReservation)
        if (!isReservation && clearDateTime) {
            dataBinding.etDate.setText("")
            dataBinding.etTime.setText("")
            viewModel.pickupDate.value = ""
            viewModel.pickupTime.value = ""
        }
    }

    private fun restorePickupFields(state: DeliveryRequestState) {
        if (state.pickupIsImmediately) {
            return
        }
        state.pickupDate?.takeIf { it.isNotBlank() }?.let {
            dataBinding.etDate.setText(formatKoreanDate(it))
            viewModel.pickupDate.value = it
        }
        state.pickupTime?.takeIf { it.isNotBlank() }?.let {
            dataBinding.etTime.setText(formatTimeToKorean(it))
            viewModel.pickupTime.value = it
        }
    }

    private fun setDateTimeSelectEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.35f
        listOf(dataBinding.layoutDate, dataBinding.layoutTime).forEach {
            it.isEnabled = enabled
            it.isClickable = enabled
            it.alpha = alpha
        }
        listOf(dataBinding.ivDate, dataBinding.ivTime, dataBinding.etDate, dataBinding.etTime).forEach {
            it.alpha = alpha
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSelectMeetType(isMeet: Boolean) {
        dataBinding.tvMeet.apply {
            isSelected = isMeet
            setTextColor(requireContext().getColor(if (isMeet) R.color.white else R.color.black_80))
        }
        dataBinding.tvNotMeet.apply {
            isSelected = !isMeet
            setTextColor(requireContext().getColor(if (!isMeet) R.color.white else R.color.black_80))
        }
        viewModel.isFaceToFace.value = isMeet
    }

    private fun moveIndicatorTo(target: View) {
        val animator =
            ObjectAnimator.ofFloat(dataBinding.tvIndicator, "translationX", target.x - 4.dpToPx())
        animator.duration = 250
        animator.start()
    }

    private fun moveIndicator2To(target: View) {
        dataBinding.tvIndicator2.post {
            val params = dataBinding.tvIndicator2.layoutParams
            params.width = target.width
            dataBinding.tvIndicator2.layoutParams = params

            val animator = ObjectAnimator.ofFloat(
                dataBinding.tvIndicator2,
                "translationX",
                target.x - 4.dpToPx()
            )
            animator.duration = 250
            animator.start()
        }
    }

    override fun initObserver() {
        super.initObserver()

        dataBinding.tvConfirm.setOnClickListener {
            sharedViewModel.updatePickupInfo(
                isImmediately = viewModel.isImmediately.value,
                date = viewModel.pickupDate.value,
                time = viewModel.pickupTime.value,
                isFaceToFace = viewModel.isFaceToFace.value
            )
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryPickupViewModel.Event.Back -> {
                    findNavController().popBackStack()
                }

                DeliveryPickupViewModel.Event.SelectDate -> {
                    if (viewModel.isImmediately.value) return@observe
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomCalendarDialog().apply {
                            selectedDate = this@DeliveryPickupFragment.viewModel.pickupDate.value.toLocalDateOrNull()
                            daySelectCallback = { selectedDate ->
                                this@DeliveryPickupFragment.dataBinding.etDate.setText(selectedDate.toKoreanDateYYYYMMDDEEEE())
                                this@DeliveryPickupFragment.viewModel.pickupDate.value = selectedDate.toYYYYMMDD()
                            }
                        }
                    )
                }

                DeliveryPickupViewModel.Event.SelectTime -> {
                    if (viewModel.isImmediately.value) return@observe
                    dialogFragmentShow(
                        childFragmentManager,
                        BottomTimeDialog().apply {
                            timeCallback = { hour24, minutes ->
                                if(hour24 == -1 && minutes == -1){ //즉시
                                    this@DeliveryPickupFragment.viewModel.pickupTime.value = "0000"
                                }else{
                                    this@DeliveryPickupFragment.dataBinding.etTime.setText(formatAmPm(hour24, minutes))
                                    this@DeliveryPickupFragment.viewModel.pickupTime.value = formatTimeValue(hour24, minutes)
                                }
                            }
                        }
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                sharedViewModel.state.collect { state ->
                    val hasPickupState = state.pickupDate != null || state.pickupTime != null
                    val isImmediately = if (hasPickupState) state.pickupIsImmediately else true
                    moveIndicatorTo(if(isImmediately)dataBinding.tvImmediate else dataBinding.tvReservation)
                    setSelectPickupType(isReservation = !isImmediately, clearDateTime = false)

                    restorePickupFields(state)
                    val isFaceToFace = if (hasPickupState) state.pickupIsFaceToFace else true
                    moveIndicator2To(if(isFaceToFace)dataBinding.tvMeet else dataBinding.tvNotMeet)
                    setSelectMeetType(isFaceToFace)
                }
            }
        }
    }

    private fun formatAmPm(hour24: Int, minute: Int): String {
        val amPm = if (hour24 < 12) "오전" else "오후"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return "$amPm $hour12:${"%02d".format(minute)}"
    }

    private fun formatTimeValue(hour24: Int, minute: Int): String {
        return "%02d%02d".format(hour24, minute)
    }

    private fun String?.toLocalDateOrNull(): LocalDate? {
        if (this.isNullOrBlank()) return null
        return runCatching {
            LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
        }.getOrNull()
    }

    fun formatKoreanDate(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val outputFormatter = DateTimeFormatter.ofPattern(
            "yyyy년 MM월 dd일 EEEE",
            Locale.KOREAN
        )

        val date = LocalDate.parse(input, inputFormatter)
        return date.format(outputFormatter)
    }

    fun formatTimeToKorean(time: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("HHmm")
        val outputFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        val normalizedTime = time.padStart(4, '0').takeLast(4)

        val localTime = LocalTime.parse(normalizedTime, inputFormatter)
        return localTime.format(outputFormatter)
    }
}
