@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.to_doappbyorlov

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.to_doappbyorlov.ui.TaskList
import com.example.to_doappbyorlov.ui.theme.DiceGameTheme
import com.example.to_doappbyorlov.ui.theme.EditTaskFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import android.os.Build

class MainActivity : AppCompatActivity() {
    private val gson = Gson()
    private val prefs by lazy {
        getSharedPreferences("todo_prefs", MODE_PRIVATE)
    }
    private var tasksUpdateTrigger = 0

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("task", Task::class.java)
            } else {
            @Suppress("DEPRECATION")
                result.data?.getParcelableExtra("task")
            }
            task?.let { newTask ->
                TaskData.tasks.add(newTask)
                saveTasks()
                tasksUpdateTrigger++

                var tagsUpdated = false
                newTask.tags.forEach { tag ->
                    val existingTag = TaskData.allTags.find { it.equals(tag, ignoreCase = true) }
                    if (existingTag == null && tag.isNotBlank()) {
                        TaskData.allTags.add(tag)
                        tagsUpdated = true
                    }
                }
                if (tagsUpdated) {
                    TaskData.allTags.sort()
                    saveTags()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTasks()
        loadTags()

        setContent {
            DiceGameTheme {
            var searchQuery by remember { mutableStateOf("") }
                var selectedTag by remember { mutableStateOf<String?>(null) }
                var expanded by remember { mutableStateOf(false) }

                val filteredTasks = remember(searchQuery, selectedTag, tasksUpdateTrigger, TaskData.tasks) {
                    TaskData.tasks.filter { task ->
                        val matchesSearch = searchQuery.isEmpty() || 
                            task.description.contains(searchQuery, ignoreCase = true) ||
                            task.tags.any { it.contains(searchQuery, ignoreCase = true) }
                        
                        val matchesTag = selectedTag == null || 
                            task.tags.any { it.equals(selectedTag, ignoreCase = true) }
                        
                        matchesSearch && matchesTag
                    }
                }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                val intent = Intent(this, SecondActivity::class.java)
                                addLauncher.launch(intent)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Додати завдання")
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Пошук завдань...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Пошук")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистити")
                                    }
                                }
                            },
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedTag ?: "Всі теги",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Відкрити список тегів")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Всі теги") },
                                    onClick = {
                                        selectedTag = null
                                        expanded = false
                                    }
                                )
                                TaskData.allTags.sorted().forEach { tag ->
                                    DropdownMenuItem(
                                        text = { Text(tag) },
                                        onClick = {
                                            selectedTag = tag
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            TaskList(
                                tasks = filteredTasks,
                                onDelete = { task ->
                                    TaskData.tasks.remove(task)
                                    saveTasks()
                                    tasksUpdateTrigger++
                                },
                                onEdit = { task ->
                                    showEditTaskFragment(task)
                            }
                            )
                        }
                    }
                }
            }
        }

        supportFragmentManager.setFragmentResultListener("task_updated", this) { _, bundle ->
            val updatedTask = bundle.getParcelable("updated_task") as? Task
            updatedTask?.let {
                val index = TaskData.tasks.indexOfFirst { task -> task.id == it.id }
                if (index != -1) {
                    TaskData.tasks[index] = it
                    saveTasks()
                    tasksUpdateTrigger++
                }
            }
        }
    }

    private fun showEditTaskFragment(task: Task) {
        val fragment = EditTaskFragment().apply {
            arguments = Bundle().apply {
                putParcelable("task", task)
            }
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun saveTasks() {
        try {
            val json = gson.toJson(TaskData.tasks)
            prefs.edit().putString("tasks", json).apply()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving tasks", e)
        }
    }

    private fun loadTasks() {
        prefs.getString("tasks", null)?.let { json ->
            try {
                val type = object : TypeToken<List<Task>>() {}.type
                val list: List<Task> = gson.fromJson(json, type) ?: emptyList()
                TaskData.tasks.clear()
                TaskData.tasks.addAll(list)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading tasks", e)
                prefs.edit().remove("tasks").apply()
            }
        }
    }

    fun saveTags() {
        try {
            val tagsToSave = TaskData.allTags.toList()
            val json = gson.toJson(tagsToSave)
            prefs.edit().putString("all_tags", json).apply()
            Log.d("MainActivity", "Tags saved: $json")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving tags", e)
        }
    }

    private fun loadTags() {
        prefs.getString("all_tags", null)?.let { json ->
            Log.d("MainActivity", "Loading tags: $json")
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val list: List<String> = gson.fromJson(json, type) ?: emptyList()
                TaskData.allTags.clear()
                TaskData.allTags.addAll(list)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading tags", e)
                prefs.edit().remove("all_tags").apply()
            }
        }
    }
}