package com.example.to_doappbyorlov

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Task(
    val id: UUID = UUID.randomUUID(),
    val description: String,
    val tags: List<String>,
    val dueDate: Long
) : Parcelable
