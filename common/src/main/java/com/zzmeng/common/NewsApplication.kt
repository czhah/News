package com.zzmeng.common

import android.app.Application

class NewsApplication : Application() {

    companion object {
        lateinit var mNewsApplication: NewsApplication
    }

    override fun onCreate() {
        super.onCreate()
        mNewsApplication = this

    }

}
