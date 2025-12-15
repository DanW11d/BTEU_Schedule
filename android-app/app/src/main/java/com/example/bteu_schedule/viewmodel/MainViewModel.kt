package com.example.bteu_schedule.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bteu_schedule.ui.theme.ThemeManager
import com.example.bteu_schedule.ui.theme.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val themeManager: ThemeManager,
    val languageManager: LanguageManager
) : ViewModel()
