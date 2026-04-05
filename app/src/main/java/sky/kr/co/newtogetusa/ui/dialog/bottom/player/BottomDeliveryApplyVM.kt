package sky.kr.co.newtogetusa.ui.dialog.bottom.player

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomDeliveryApplyVM @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory) :
    BaseViewModel(baseViewModelDependenciesFactory.create()) {
}