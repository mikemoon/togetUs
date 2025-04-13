package sky.kr.co.newtogetusa.ui.dialog.bottom

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class BottomCalendarViewModel @Inject constructor(baseViewModelFactory: BaseViewModelDependenciesFactory): BaseViewModel(baseViewModelFactory.create()) {
}