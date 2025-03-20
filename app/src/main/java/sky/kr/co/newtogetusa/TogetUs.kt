package sky.kr.co.newtogetusa

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TogetUs : Application(){

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

    }
}