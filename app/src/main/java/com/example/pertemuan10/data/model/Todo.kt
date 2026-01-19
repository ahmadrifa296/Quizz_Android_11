package com.example.pertemuan10.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.PropertyName

enum class Priority(val color: Color, val label: String, val level: Int) {
    HIGH(Color(0xFFEF5350), "High", 3),
    MEDIUM(Color(0xFFFFB74D), "Medium", 2),
    LOW(Color(0xFF81C784), "Low", 1);

    companion object {
        fun fromString(value: String) = entries.find { it.name.equals(value, true) } ?: MEDIUM
    }
}

data class Todo(
    val id: String = "",
    val title: String = "",
    val priority: String = "Medium",
    val category: String = "Kuliah",
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)