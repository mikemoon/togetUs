package sky.kr.co.newtogetusa.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
open class BaseViewModel @Inject constructor(private val dependencies: BaseViewModelDependencies) : ViewModel() {
    protected val dataStoreRepository = dependencies.dataStoreRepository

    var accessToken:String = ""
    init {
        viewModelScope.launch {
            accessToken = dataStoreRepository.getString(DataStoreKey.KEY_TOKEN).orEmpty()
        }
    }
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

