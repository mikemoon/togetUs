package sky.kr.co.newtogetusa.ui.dialog.message

import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class MessageDialogViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory):
    BaseViewModel(baseViewModelFactory.create()){

    var imageResId = MutableStateFlow(0)

    private val _leftClick = SingleLiveEvent<Unit>()
    val leftClick: LiveData<Unit> get() = _leftClick

    private val _rightClick = SingleLiveEvent<Unit>()
    val rightClick: LiveData<Unit> get() = _rightClick

    fun onLeftClick() {
        _leftClick.call()
    }

    fun onRightClick() {
        _rightClick.call()
    }
}