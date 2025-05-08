@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.to_doappbyorlov.ui.theme

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.to_doappbyorlov.Task
import com.example.to_doappbyorlov.TaskData
import com.example.to_doappbyorlov.ui.theme.DiceGameTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import android.widget.Toast
import androidx.compose.ui.graphics.Color

class EditTaskFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val task = arguments?.getParcelable<Task>("task")
        if (task == null) {
            return ComposeView(requireContext()).apply {
                setContent {
                    DiceGameTheme {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Помилка, завдання не знайдено",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                DiceGameTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Редагувати завдання",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    IconButton(onClick = { parentFragmentManager.popBackStack() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Закрити")
                                    }
                                }
                                EditTaskScreen(
                                    task = task,
                                    onSave = { updatedTask ->
                                        parentFragmentManager.setFragmentResult("task_updated", Bundle().apply {
                                            putParcelable("updated_task", updatedTask)
                                        })
                                        parentFragmentManager.popBackStack()
                                    },
                                    onCancel = {
                                        parentFragmentManager.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(task: Task?, onSave: (Task) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var description by remember { mutableStateOf(TextFieldValue(task?.description ?: "")) }
    var customTag by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTags by remember { mutableStateOf<Set<String>>(task?.tags?.toSet() ?: emptySet()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val onDatePicked = rememberUpdatedState<(Int, Int, Int) -> Unit> { year, month, day ->
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.timeInMillis = it.timeInMillis }
        calendar.set(year, month, day)
        selectedDate = calendar
    }
    val onTimePicked = rememberUpdatedState<(Int, Int) -> Unit> { hour, minute ->
        val calendar = Calendar.getInstance()
        selectedTime?.let { calendar.timeInMillis = it.timeInMillis }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        selectedTime = calendar
    }

    LaunchedEffect(task) {
        if (task != null) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = task.dueDate
            }
            selectedDate = calendar
            selectedTime = calendar
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { 
                if (it.text.length <= 50) {
                    description = it
                }
            },
            label = { Text("Назва завдання") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "Теги",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(TaskData.allTags.sorted()) { tag ->
                val isSelected = tag in selectedTags
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (!isSelected && selectedTags.size >= 3) {
                                Toast.makeText(context, "Можна вибрати максимум 3 теги", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                                if (selectedTags.isNotEmpty()) {
                                    customTag = TextFieldValue("")
                                }
                            }
                        },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        OutlinedTextField(
            value = customTag,
            onValueChange = { 
                if (it.text.length <= 35) {
                    customTag = it
                }
            },
            label = { Text("Власний тег") },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedTags.isEmpty()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedDate?.let { 
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it.time)
                } ?: "Виберіть дату")
            }

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedTime?.let { 
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it.time)
                } ?: "Виберіть час")
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (description.text.isEmpty()) {
                        errorMessage = "Введіть назву завдання"
                        return@Button
                    }

                    if (selectedDate == null || selectedTime == null) {
                        errorMessage = "Виберіть дату та час виконання"
                        return@Button
                    }

                    val finalDateTime = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedDate!!.get(Calendar.YEAR))
                        set(Calendar.MONTH, selectedDate!!.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, selectedDate!!.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val now = Calendar.getInstance()
                    if (finalDateTime.timeInMillis <= now.timeInMillis) {
                        errorMessage = "Дата та час мають бути в майбутньому"
                        return@Button
                    }

                    val customTagText = customTag.text.trim()
                    val finalTags = mutableSetOf<String>().apply {
                        addAll(selectedTags)
                        if (customTagText.isNotEmpty()) {
                            add(customTagText)
                            if (!TaskData.allTags.contains(customTagText)) {
                                TaskData.allTags.add(customTagText)
                            }
                        }
                    }

                    if (finalTags.isEmpty()) {
                        errorMessage = "Оберіть або введіть хоча б один тег"
                        return@Button
                    }

                    val updatedTask = Task(
                        id = task?.id ?: UUID.randomUUID(),
                        description = description.text,
                        tags = finalTags.toList(),
                        dueDate = finalDateTime.timeInMillis
                    )
                    onSave(updatedTask)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Зберегти")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Скасувати")
            }
        }
    }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            selectedDate?.let { calendar.timeInMillis = it.timeInMillis }
            val dialog = DatePickerDialog(
                context,
                { _, year, month, day ->
                    onDatePicked.value(year, month, day)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnCancelListener { showDatePicker = false }
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
        }
    }
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            val calendar = Calendar.getInstance()
            selectedTime?.let { calendar.timeInMillis = it.timeInMillis }
            val dialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    onTimePicked.value(hour, minute)
                    showTimePicker = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            dialog.setOnCancelListener { showTimePicker = false }
            dialog.setOnDismissListener { showTimePicker = false }
            dialog.show()
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
           cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}
