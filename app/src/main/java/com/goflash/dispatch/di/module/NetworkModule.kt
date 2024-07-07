package com.goflash.dispatch.di.module

import android.app.Application
import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.goflash.dispatch.BuildConfig
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.api_services.SessionService
import com.goflash.dispatch.api_services.SortationServices
import com.goflash.dispatch.app_constants.assetId
import com.goflash.dispatch.app_constants.authKey
import com.goflash.dispatch.util.DateSerializer
import com.goflash.dispatch.util.PreferenceHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by Ravi on 28/05/19.
 */
@Module
class NetworkModule(var url: String, val context: Context) {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().registerTypeAdapter(Calendar::class.java, DateSerializer()).serializeNulls().setLenient().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        val okHttpBuilder = OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()

            val shouldAddAuthHeaders = original.headers["isAuthorizable"] != "false"


            val requestBuilder: Request.Builder
            val originalHttpUrl = original.url
            val url = originalHttpUrl.newBuilder()
                .build()
            requestBuilder = if (SessionService.token.isNotEmpty()) {
                //Log.d("Authorization", SessionService.token)
                original.newBuilder()
                    .header(assetId, PreferenceHelper.assignedAssetId.toString())
                    .removeHeader("isAuthorizable")
                    .url(url)

            } else {
                //Log.d("Authorization 2", SessionService.token)
                original.newBuilder()
                    .url(url)
            }

            if(shouldAddAuthHeaders)
                requestBuilder.addHeader(authKey, PreferenceHelper.token)
                    .addHeader("Content-Type", "application/json")

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        return okHttpBuilder.addInterceptor(loggingInterceptor)
            .addInterceptor(ChuckerInterceptor(context))
            .connectTimeout(5, TimeUnit.MINUTES) // Change it as per your requirement
            .readTimeout(5, TimeUnit.MINUTES)// Change it as per your requirement
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }


    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(okHttpClient)
            .baseUrl(url)
            .build()
    }

    @Provides
    @Singleton
    fun provideBoltApiService(retrofit: Retrofit): SortationServices {
        return retrofit.create(SortationServices::class.java)
    }

}