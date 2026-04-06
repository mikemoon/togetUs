package sky.kr.co.newtogetusa.ui.main.history.player

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependencies
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class PlayerDeliveryNoticeVM @Inject constructor(baseViewModelDependenciesFactory: BaseViewModelDependenciesFactory)
    : BaseViewModel(baseViewModelDependenciesFactory.create()){
}