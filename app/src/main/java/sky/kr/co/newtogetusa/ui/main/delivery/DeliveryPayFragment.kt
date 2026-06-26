package sky.kr.co.newtogetusa.ui.main.delivery

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.portone.sdk.android.PortOne
import io.portone.sdk.android.payment.PaymentCallback
import io.portone.sdk.android.type.entity.Currency
import io.portone.sdk.android.type.entity.PaymentPayMethod
import io.portone.sdk.android.type.request.PaymentRequest
import io.portone.sdk.android.type.response.PaymentResponse
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.FragmentDeliveryPayBinding
import sky.kr.co.newtogetusa.ui.base.BaseFragment
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomCalendarDialog
import sky.kr.co.newtogetusa.ui.dialog.bottom.BottomTimeDialog
import sky.kr.co.newtogetusa.utils.toast
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class DeliveryPayFragment : BaseFragment<FragmentDeliveryPayBinding, DeliveryPayViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_delivery_pay
    override val viewModel: DeliveryPayViewModel by viewModels()
    private val args: DeliveryPayFragmentArgs by navArgs()

    private val paymentActivityResultLauncher =
        PortOne.registerForPaymentActivity(
            fragment = this,
            callback = object : PaymentCallback {
                override fun onSuccess(response: PaymentResponse) {
                    Timber.d("Payment success paymentId=${response.paymentId}, txId=${response.txId}")
                    val paymentId = response.paymentId
                    if (paymentId.isNullOrBlank()) {
                        requireContext().toast("결제 정보를 확인할 수 없습니다.")
                        return
                    }
                    viewModel.confirmPayment(paymentId)
                }

                override fun onFail(response: PaymentResponse) {
                    Timber.e(
                        "Payment failed code=${response.code}, message=${response.message}, pgCode=${response.pgCode}, pgMessage=${response.pgMessage}, paymentId=${response.paymentId}"
                    )
                    requireContext().toast(response.message ?: "결제에 실패했습니다.")
                }
            }
        )

    override fun init() {
        super.init()
        dataBinding.viewModel = viewModel
        viewModel.load(args.deliveryId, args.playerId)
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                DeliveryPayViewModel.Event.Back -> findNavController().popBackStack()
                DeliveryPayViewModel.Event.SelectPickupDate -> showPickupDateDialog()
                DeliveryPayViewModel.Event.SelectPickupTime -> showPickupTimeDialog()
                is DeliveryPayViewModel.Event.StartPortOnePayment -> {
                    startPortOnePayment(event.config.storeId, event.config.channelKey, event.amount)
                }
                DeliveryPayViewModel.Event.PaymentSuccess -> {
                    requireContext().toast("결제가 완료되었습니다.")
                    findNavController().popBackStack()
                }
                is DeliveryPayViewModel.Event.ShowMessage -> requireContext().toast(event.message)
            }
        }
    }

    private fun startPortOnePayment(storeId: String, channelKey: String, amount: Long) {
        val paymentId = UUID.randomUUID().toString().replace("-", "")
        PortOne.requestPayment(
            activity = requireActivity(),
            request = PaymentRequest(
                storeId = storeId,
                paymentId = paymentId,
                orderName = "투겟어스 동행요금",
                totalAmount = amount,
                currency = Currency.KRW,
                payMethod = PaymentPayMethod.CARD,
                channelKey = channelKey,
                appScheme = "togetus"
            ),
            resultLauncher = paymentActivityResultLauncher
        )
    }

    private fun showPickupDateDialog() {
        BottomCalendarDialog().apply {
            selectedDate = this@DeliveryPayFragment.viewModel.selectedPickupDate()
            daySelectCallback = { date ->
                this@DeliveryPayFragment.viewModel.setPickupDate(date)
            }
        }.show(parentFragmentManager, "pickupDate")
    }

    private fun showPickupTimeDialog() {
        BottomTimeDialog().apply {
            allowNegotiable = false
            timeCallback = { hour, minute ->
                this@DeliveryPayFragment.viewModel.setPickupTime(hour, minute)
            }
        }.show(parentFragmentManager, "pickupTime")
    }
}
