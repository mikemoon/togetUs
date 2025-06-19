package sky.kr.co.newtogetusa.ui.others.terms

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.base.SingleLiveEvent
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class PlayerTermCompleteViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {

    private var _moveToHome = SingleLiveEvent<Boolean>()
    val moveToHome : SingleLiveEvent<Boolean> get() = _moveToHome
    fun onMoveToHome(){
        _moveToHome.value = true
    }
}