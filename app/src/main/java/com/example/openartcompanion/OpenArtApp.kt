package com.example.openartcompanion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp()
class OpenArtApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
