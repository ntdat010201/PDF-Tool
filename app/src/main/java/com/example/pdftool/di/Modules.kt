package com.example.pdftool.di

import com.example.pdftool.domain.activities.MainActivity
import org.koin.dsl.module

val mainActivity = module {
    scope<MainActivity> {
    }
}

val listModule = listOf(
    mainActivity,
)