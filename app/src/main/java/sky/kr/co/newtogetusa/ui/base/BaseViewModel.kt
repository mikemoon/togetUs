package sky.kr.co.newtogetusa.ui.base

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
open class BaseViewModel @Inject constructor(private val dependencies: BaseViewModelDependencies) : ViewModel() {
    protected val dataStoreRepository = dependencies.dataStoreRepository
    open fun init() {}
}


@Singleton
class BaseViewModelDependenciesFactory @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
) {
    fun create(): BaseViewModelDependencies {
        return BaseViewModelDependencies(
            dataStoreRepository = dataStoreRepository
        )
    }
}

class BaseViewModelDependencies @Inject constructor(
    val dataStoreRepository: DataStoreRepository,
)

