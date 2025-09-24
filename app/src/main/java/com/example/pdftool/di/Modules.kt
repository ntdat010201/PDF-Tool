package com.example.pdftool.di

import com.example.pdftool.domain.activities.MainActivity
import com.example.pdftool.domain.fragment.HomeFragment
import com.example.pdftool.domain.fragment.RecentFragment
import com.example.pdftool.domain.fragment.BookmarksFragment
import com.example.pdftool.viewmodel.FileViewModel
import com.example.pdftool.data.database.AppDatabase
import com.example.pdftool.data.repository.RecentFileRepository
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

val databaseModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().recentFileDao() }
}

val repositoryModule = module {
    single { RecentFileRepository(get()) }
}

val viewModelModule = module {
    viewModel { FileViewModel(androidContext(), get()) }
}

val listModule = listOf(
    mainActivity,
    databaseModule,
    repositoryModule,
    viewModelModule,
)