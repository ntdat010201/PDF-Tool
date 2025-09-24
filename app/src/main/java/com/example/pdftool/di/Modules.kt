package com.example.pdftool.di

import com.example.pdftool.domain.activities.MainActivity
import com.example.pdftool.domain.fragment.HomeFragment
import com.example.pdftool.domain.fragment.RecentFragment
import com.example.pdftool.domain.fragment.BookmarksFragment
import com.example.pdftool.viewmodel.FileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainActivity = module {
    scope<MainActivity> {
        scoped { HomeFragment() }
        scoped { RecentFragment() }
        scoped { BookmarksFragment() }

    }
}

val viewModelModule = module {
    viewModel { FileViewModel(androidContext()) }
}

val listModule = listOf(
    mainActivity,
    viewModelModule,
)