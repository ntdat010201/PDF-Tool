package com.example.pdftool.di

import com.example.pdftool.domain.activities.MainActivity
import com.example.pdftool.domain.fragment.HomeFragment
import com.example.pdftool.domain.fragment.RecentFragment
import com.example.pdftool.domain.fragment.BookmarksFragment
import org.koin.dsl.module

val mainActivity = module {
    scope<MainActivity> {
        scoped { HomeFragment() }
        scoped { RecentFragment() }
        scoped { BookmarksFragment() }
    }
}

val listModule = listOf(
    mainActivity,
)