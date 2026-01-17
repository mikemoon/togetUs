package sky.kr.co.newtogetusa.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sky.kr.co.newtogetusa.data.remote.api.AddressSearchService
import sky.kr.co.newtogetusa.data.remote.api.AdminApiService
import sky.kr.co.newtogetusa.data.remote.api.ConfigService
import sky.kr.co.newtogetusa.data.remote.api.DeliveryService
import sky.kr.co.newtogetusa.data.remote.api.PlayerService
import sky.kr.co.newtogetusa.data.remote.api.UserService
import sky.kr.co.newtogetusa.repository.AddressSearchRepository
import sky.kr.co.newtogetusa.repository.AdminRepository
import sky.kr.co.newtogetusa.repository.ConfigRepository
import sky.kr.co.newtogetusa.repository.DataStoreRepository
import sky.kr.co.newtogetusa.repository.DataStoreRepositoryImpl
import sky.kr.co.newtogetusa.repository.DeliveryRepository
import sky.kr.co.newtogetusa.repository.PlayerRepository
import sky.kr.co.newtogetusa.repository.UserRepository
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

    @Singleton
    fun providePlayerRepository(@NetworkModule.PlayerApi apiService: PlayerService): PlayerRepository =
        PlayerRepository(apiService)

    @Singleton
    fun provideUserRepository(@NetworkModule.UserApi apiService: UserService): UserRepository =
        UserRepository(apiService)

    @Singleton
    fun provideAdminRepository(@NetworkModule.AdminApi apiService: AdminApiService): AdminRepository =
        AdminRepository(apiService)

    @Singleton
    fun provideDeliveryRepository(@NetworkModule.DeliveryApi apiService: DeliveryService): DeliveryRepository =
        DeliveryRepository(apiService)

}