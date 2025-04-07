package sky.kr.co.newtogetusa.ui.dialog.message

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import sky.kr.co.newtogetusa.BR
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.databinding.DialogMessageBinding
import sky.kr.co.newtogetusa.ui.base.BaseDialogFragment

@AndroidEntryPoint
class MessageDialog : BaseDialogFragment<DialogMessageBinding, MessageDialogViewModel>(){
    override val layoutId: Int
        get() = R.layout.dialog_message
    override val viewModel: MessageDialogViewModel by viewModels()

    private var msg: String? = null
    private var rightBtn: String? = null
    private var leftBtn: String? = null
    private var isCancel: Boolean = true
    private var msgTitle: String? = null
    private var leftClickAction = {}
    private var rightClickAction = {}
    var isShowing = false

    companion object {
        private const val ARG_MSG = "arg_msg"
        private const val ARG_RIGHT_BTN = "arg_right_btn"
        private const val ARG_LEFT_BTN = "arg_left_btn"
        private const val ARG_IS_CANCEL = "arg_is_cancel"
        private const val ARG_MSG_TITLE = "arg_msg_title"
        private const val ARG_IMAGE_RES_ID = "arg_image_res_id"

        fun newInstance(
            msg: String,
            rightBtn: String,
            leftBtn: String = "",
            isCancel: Boolean = true,
            msgTitle: String = "",
            imageResId: Int = 0,
        ): MessageDialog {
            val dialog = MessageDialog()
            dialog.arguments = bundleOf(
                ARG_MSG to msg,
                ARG_RIGHT_BTN to rightBtn,
                ARG_LEFT_BTN to leftBtn,
                ARG_IS_CANCEL to isCancel,
                ARG_MSG_TITLE to msgTitle,
                ARG_IMAGE_RES_ID to imageResId,
            )
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            msg = it.getString(ARG_MSG)
            rightBtn = it.getString(ARG_RIGHT_BTN)
            leftBtn = it.getString(ARG_LEFT_BTN)
            isCancel = it.getBoolean(ARG_IS_CANCEL)
            msgTitle = it.getString(ARG_MSG_TITLE)
            viewModel.imageResId.value = it.getInt(ARG_IMAGE_RES_ID)
        }

        if (!isCancel) isCancelable = isCancel
        isShowing = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.setVariable(BR.msg, msg)
        dataBinding.txtDialogRight.text = rightBtn

        if (!msgTitle.isNullOrEmpty()) {
            dataBinding.tvTitle.isVisible = true
            dataBinding.tvTitle.text = msgTitle
        }else{
            dataBinding.tvMessage.apply {
                //setTextAppearance(R.style.TextmdRegular)
                //setTextColor(context.getColor(R.color.gray_700))
            }
        }

        if (!leftBtn.isNullOrEmpty()) {
            dataBinding.txtDialogLeft.text = leftBtn
            dataBinding.groupBtnOne.isVisible = true
        }
    }

    override fun initObserve() {
        viewModel.leftClick.observe(this) {
            leftClickAction.invoke()
            dismiss()
        }
        viewModel.rightClick.observe(this) {
            rightClickAction.invoke()
            dismiss()
        }
    }

    fun onLeftBtn(action: () -> (Unit) = {}): MessageDialog {
        leftClickAction = action
        return this
    }

    fun onRightBtn(action: () -> (Unit) = {}): MessageDialog {
        rightClickAction = action
        return this
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isShowing = false
    }
}