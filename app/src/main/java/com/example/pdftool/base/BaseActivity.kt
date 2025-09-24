package com.example.pdftool.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.example.pdftool.di.listModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.activityScope
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

open class BaseActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(applicationContext)
                modules(listModule)
            }
        }
        setupKoinFragmentFactory(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.close()
    }
}
