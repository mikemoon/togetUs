package sky.kr.co.newtogetusa.repository

import sky.kr.co.newtogetusa.data.remote.BaseNetRepo
import sky.kr.co.newtogetusa.data.remote.api.AuthService
import sky.kr.co.newtogetusa.di.NetworkModule
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @NetworkModule.ApiServer private val authService: AuthService
): BaseNetRepo() {
}