package com.zzmeng.common.http

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.zzmeng.common.NewsApplication
import com.zzmeng.common.utils.NetWorkUtils
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 请求
 */
class RetrofitManager {
    companion object {

        private const val NETWORK_CACHE_SIZE = 10 * 1024 * 1024L

        private val cacheControlInterceptor:Interceptor =

                Interceptor { chain ->
                    var request = chain.request()
                    if (!NetWorkUtils.isNetworkConnected(NewsApplication.mNewsApplication)) {
                        //  没有网络 读取缓存
                        request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
                    }

                    val originalResponse = chain.proceed(request)

                    if (NetWorkUtils.isNetworkConnected(NewsApplication.mNewsApplication)) {
                        //  有网络时 设置缓存
                        val maxAge = 0 // read from cache
                        originalResponse.newBuilder()
                                .header("Cache-Control", "public ,max-age=$maxAge")
                                .removeHeader("Pragma")
                                //                        .body(new ProgressResponseBody(originalResponse.body()))
                                .build()
                    } else {
                        //  没有网络时 直接读取缓存
                        val maxStale = 60 * 60 * 24
                        originalResponse.newBuilder()
                                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                                .removeHeader("Pragma")
                                .build()
                    }
                }

        fun getRetrofit(): Retrofit{
            val file = File(NewsApplication.mNewsApplication.cacheDir, "httpclient")
            val cache = Cache(file, NETWORK_CACHE_SIZE)

            val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(NewsApplication.mNewsApplication))
            val builder = OkHttpClient.Builder()
                    .cache(cache)
                    .cookieJar(cookieJar)
                    .addInterceptor(cacheControlInterceptor)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)//  失败重连

            //  测试包打开日志
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)

            return Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }
    }
}