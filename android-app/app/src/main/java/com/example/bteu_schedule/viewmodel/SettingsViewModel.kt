package com.example.bteu_schedule.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bteu_schedule.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val themeManager: ThemeManager
) : ViewModel()
