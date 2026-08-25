package com.caiji.app

import android.app.Application

class CaiJiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CaiJiApplication
            private set
    }
}
