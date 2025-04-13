package sky.kr.co.newtogetusa.ui.main.delivery

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class DeliveryMapViewModel @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory):BaseViewModel(baseViewModelDependenciesFactory.create()) {
}