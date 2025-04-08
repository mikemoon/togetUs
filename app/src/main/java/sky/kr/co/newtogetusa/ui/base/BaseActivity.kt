package sky.kr.co.newtogetusa.ui.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import sky.kr.co.newtogetusa.BR

abstract class BaseActivity <T : ViewDataBinding, E : BaseViewModel> : AppCompatActivity(){
    lateinit var dataBinding: T
    abstract val layoutId: Int
    abstract val viewModel: E

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }catch (_: Exception){
        }
        dataBinding = DataBindingUtil.setContentView(this, layoutId)
        dataBinding.lifecycleOwner = this
        dataBinding.setVariable(BR.viewModel, viewModel)

        init()
        initObserver()
    }

    open fun init(){}
    open fun initObserver(){}
}