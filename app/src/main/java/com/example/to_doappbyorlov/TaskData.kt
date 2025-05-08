package com.example.to_doappbyorlov

import androidx.compose.runtime.mutableStateListOf

object TaskData {
    val tasks = mutableStateListOf<Task>()

    val allTags = mutableStateListOf<String>()
}