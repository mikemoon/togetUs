package sky.kr.co.newtogetusa.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sky.kr.co.newtogetusa.repository.KakaoLocalRepository
import sky.kr.co.newtogetusa.repository.KakaoLocalRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryBindModule {

    @Binds
    @Singleton
    abstract fun bindKakaoLocalRepository(
        impl: KakaoLocalRepositoryImpl
    ): KakaoLocalRepository
}