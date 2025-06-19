package sky.kr.co.newtogetusa.ui.dialog.bottom

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomFilterViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

       private var _closeEvent = SingleLiveEvent<Unit>()
    val closeEvent get() = _closeEvent
    fun onClose(){
        _closeEvent.value = Unit
    }
}