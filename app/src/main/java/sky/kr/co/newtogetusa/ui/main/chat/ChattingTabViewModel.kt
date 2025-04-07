package sky.kr.co.newtogetusa.ui.main.chat

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class ChattingTabViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory) : BaseViewModel(baseViewModelFactory.create()) {

}