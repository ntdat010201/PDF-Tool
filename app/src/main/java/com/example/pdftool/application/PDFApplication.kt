package com.example.pdftool.application

import android.app.Application
import com.example.pdftool.di.listModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PDFApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PDFApplication)
            modules(listModule)
        }
    }

}