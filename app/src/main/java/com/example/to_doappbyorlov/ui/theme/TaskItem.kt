package com.example.to_doappbyorlov.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.to_doappbyorlov.Task
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun TaskItem(
    task: Task,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = task.description)
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Видалити")
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = "Теги: ${task.tags.joinToString()}")
            Spacer(Modifier.height(4.dp))
            val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(task.dueDate))
            Text(text = "До: $date")
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Підтвердження видалення") },
                text = { Text("Ви дійсно хочете видалити це завдання?") },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        onDelete?.invoke()
                    }) {
                        Text("Так")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Ні")
                    }
                }
            )
        }
    }
}
