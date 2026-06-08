package com.example.englishflashcard

import android.app.Application
import com.example.englishflashcard.di.AppModule

class EnglishFlashCardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}
