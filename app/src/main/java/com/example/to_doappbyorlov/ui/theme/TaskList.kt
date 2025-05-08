package com.example.to_doappbyorlov.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.to_doappbyorlov.Task

@Composable
fun TaskList(
    tasks: List<Task>,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Немає завдань. Натисніть + щоб додати нове завдання.",
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onDelete = { onDelete(task) },
                    onEdit = { onEdit(task) }
                )
            }
        }
    }
} 