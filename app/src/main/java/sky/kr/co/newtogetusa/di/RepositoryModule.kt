package sky.kr.co.newtogetusa.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sky.kr.co.newtogetusa.data.remote.api.AddressSearchService
import sky.kr.co.newtogetusa.data.remote.api.ConfigService
import sky.kr.co.newtogetusa.repository.AddressSearchRepository
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import sky.kr.co.newtogetusa.repository.DataStoreRepositoryImpl
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideDataStoreRepository(@ApplicationContext app: Context): DataStoreRepository =
        DataStoreRepositoryImpl(app)

    @Singleton
    @Provides
    fun provideAddressSearchRepository(@NetworkModule.AddressApiServer apiService: AddressSearchService): AddressSearchRepository =
        AddressSearchRepository(apiService)

    @Singleton
    @Provides
    fun provideConfigRepository(@NetworkModule.ConfigApi apiService: ConfigService): ConfigRepository =
        ConfigRepository(apiService)

}