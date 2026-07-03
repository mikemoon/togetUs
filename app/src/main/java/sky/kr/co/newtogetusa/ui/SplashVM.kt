package sky.kr.co.newtogetusa.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import sky.kr.co.newtogetusa.data.TokenStore
import sky.kr.co.newtogetusa.repository.DataStoreKey
import sky.kr.co.newtogetusa.ui.base.BaseViewModel
import sky.kr.co.newtogetusa.ui.base.BaseViewModelDependenciesFactory
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject constructor(
    baseViewModelFactory: BaseViewModelDependenciesFactory,
    private val tokenStore: TokenStore,
) :
    BaseViewModel(baseViewModelFactory.create()) {

    suspend fun resolveStartDestination(): StartDestination {
        val accessToken = dataStoreRepository.getString(DataStoreKey.KEY_TOKEN).orEmpty()
        val refreshToken = dataStoreRepository.getString(DataStoreKey.KEY_REFRESH_TOKEN).orEmpty()
        tokenStore.setTokens(accessToken, refreshToken)

        val isFirstRun = dataStoreRepository.getBoolean(DataStoreKey.KEY_IS_FIRST_RUN) ?: true
        if (isFirstRun) {
            return StartDestination.FirstRunLogin
        }

        val loginType = dataStoreRepository.getInt(DataStoreKey.RECENT_LOGIN_TYPE) ?: -1
        return if (loginType != -1 && refreshToken.isNotEmpty()) {
            StartDestination.Main
        } else {
            StartDestination.Login
        }
    }

    enum class StartDestination {
        FirstRunLogin,
        Login,
        Main,
    }
}
