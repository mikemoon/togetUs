package sky.kr.co.newtogetusa

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import sky.kr.co.newtogetusa.utils.KakaoMapSupport
import timber.log.Timber

@HiltAndroidApp
class TogetUs : Application(){

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        KakaoSdk.init(this, "1988aea67c089f06776b08aae6192b7b")
        runCatching {
            KakaoMapSdk.init(this, "1988aea67c089f06776b08aae6192b7b")
        }.onSuccess {
            KakaoMapSupport.markAvailable()
        }.onFailure {
            KakaoMapSupport.markUnavailable(it)
        }
    }

    companion object{

    }
}
