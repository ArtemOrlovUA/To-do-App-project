package com.example.to_doappbyorlov

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.to_doappbyorlov.databinding.ActivitySecondBinding
import com.example.to_doappbyorlov.databinding.ItemTagBinding
import com.google.gson.Gson
import java.util.*

class SecondActivity : ComponentActivity() {

    private lateinit var binding: ActivitySecondBinding
    private lateinit var tagAdapter: TagAdapter
    private var selectedTags = mutableSetOf<Int>()
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setBackgroundColor(android.graphics.Color.WHITE)

        val maxDescriptionFilter = InputFilter.LengthFilter(50)
        val maxLengthFilter = InputFilter.LengthFilter(35)
        binding.etDescription.filters = arrayOf(maxDescriptionFilter)
        binding.etNewTag.filters = arrayOf(maxLengthFilter)

        var dueDateMillis = 0L
        val tagsList = TaskData.allTags.sorted()
        Log.d("SecondActivity", "Loaded tags: $tagsList")

        // Setup RecyclerView
        binding.rvTags.layoutManager = LinearLayoutManager(this)
        tagAdapter = TagAdapter(tagsList.toMutableList(), selectedTags, object : TagAdapter.OnTagSelectedListener {
            override fun onTagSelected(position: Int, isSelected: Boolean) {
                if (isSelected) {
                    if (selectedTags.size >= 3) {
                        Toast.makeText(this@SecondActivity, "Можна вибрати максимум 3 теги", Toast.LENGTH_SHORT).show()
                        tagAdapter.notifyItemChanged(position)
                        return
                    }
                    selectedTags.add(position)
                } else {
                    selectedTags.remove(position)
                }
                tagAdapter.notifyDataSetChanged()
                if (selectedTags.isNotEmpty()) {
                    binding.etNewTag.setText("")
                    binding.etNewTag.isEnabled = false
                } else {
                    binding.etNewTag.isEnabled = true
                }
            }
        })
        binding.rvTags.adapter = tagAdapter

        binding.etNewTag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val customNotEmpty = !s.isNullOrBlank()
                if (customNotEmpty) {
                    if (selectedTags.isNotEmpty()) {
                        selectedTags.clear()
                        tagAdapter.notifyDataSetChanged()
                    }
                    tagAdapter.setItemsEnabled(!customNotEmpty)
                } else {
                    tagAdapter.setItemsEnabled(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDate?.let { cal.timeInMillis = it.timeInMillis }
            
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    selectedDate = cal
                    updateDateTimeDisplay()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedTime?.let { cal.timeInMillis = it.timeInMillis }
            
            TimePickerDialog(
                this,
                { _, h, m ->
                    cal.set(Calendar.HOUR_OF_DAY, h)
                    cal.set(Calendar.MINUTE, m)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    selectedTime = cal
                    updateDateTimeDisplay()
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(this, "Введіть назву завдання", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "Виберіть дату та час виконання", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                Toast.makeText(this, "Дата та час мають бути в майбутньому", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedTagsList = selectedTags.map { tagAdapter.getTag(it) }.filterNotNull()
            val customTag = binding.etNewTag.text.toString().trim()

            val finalTags = when {
                selectedTagsList.isNotEmpty() -> selectedTagsList
                customTag.isNotEmpty() -> listOf(customTag)
                else -> {
                    Toast.makeText(this, "Оберіть або введіть тег", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val task = Task(
                description = description,
                tags = finalTags,
                dueDate = finalDateTime.timeInMillis
            )
            val resultIntent = Intent().putExtra("task", task)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateDateTimeDisplay() {
        val dateStr = selectedDate?.let { 
            android.text.format.DateFormat.format("dd-MM-yyyy", it)
        } ?: "дату не обрано"
        
        val timeStr = selectedTime?.let {
            android.text.format.DateFormat.format("HH:mm", it)
        } ?: "час не обрано"
        
        binding.tvDateTime.text = "$dateStr $timeStr"
    }

    class TagAdapter(
        private val tags: MutableList<String>,
        private val selectedTags: MutableSet<Int>,
        private val listener: OnTagSelectedListener
    ) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

        private var itemsEnabled = true

        interface OnTagSelectedListener {
            fun onTagSelected(position: Int, isSelected: Boolean)
        }

        fun clearSelection() {
            notifyDataSetChanged()
        }

        fun setItemsEnabled(enabled: Boolean) {
            if (itemsEnabled != enabled) {
                itemsEnabled = enabled
                notifyDataSetChanged()
            }
        }

        fun getTag(position: Int): String? {
            return if (position in 0 until tags.size) tags[position] else null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val binding = ItemTagBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TagViewHolder(binding)
        }

        override fun getItemCount(): Int = tags.size

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            holder.bind(tags[position], position in selectedTags, itemsEnabled)
        }

        inner class TagViewHolder(private val binding: ItemTagBinding) : 
                RecyclerView.ViewHolder(binding.root) {
            
            init {
                binding.radioTag.setOnClickListener {
                    if (!itemsEnabled) return@setOnClickListener
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val isCurrentlySelected = position in selectedTags
                        listener.onTagSelected(position, !isCurrentlySelected)
                    }
                }
                binding.btnDeleteTag.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val tagToRemove = tags[position]
                        tags.removeAt(position)
                        TaskData.allTags.remove(tagToRemove)
                        val prefs = itemView.context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
                        val json = Gson().toJson(TaskData.allTags.toList())
                        prefs.edit().putString("all_tags", json).apply()
                        selectedTags.remove(position)
                        val toShift = selectedTags.filter { it > position }
                        toShift.sortedDescending().forEach { idx ->
                            selectedTags.remove(idx)
                            selectedTags.add(idx - 1)
                        }
                        notifyItemRemoved(position)
                        notifyDataSetChanged()
                    }
                }
            }
            
            fun bind(tag: String, isSelected: Boolean, enabled: Boolean) {
                binding.radioTag.text = tag
                binding.radioTag.isChecked = isSelected
                binding.radioTag.isEnabled = enabled
                itemView.isEnabled = enabled
            }
        }
    }
}