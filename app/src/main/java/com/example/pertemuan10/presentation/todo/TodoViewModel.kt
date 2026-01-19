package com.example.pertemuan10.presentation.todo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pertemuan10.data.model.Priority
import com.example.pertemuan10.data.model.Todo
import com.example.pertemuan10.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {
    private val repository = TodoRepository()

    private val _rawTodos = MutableStateFlow<List<Todo>>(emptyList())

    // State untuk Pencarian dan Filter
    val searchQuery = mutableStateOf("")
    val selectedFilter = mutableStateOf<Priority?>(null)

    // Gabungkan data mentah dengan filter (UI State)
    private val _filteredTodos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _filteredTodos.asStateFlow()

    fun observeTodos(userId: String) {
        viewModelScope.launch {
            repository.getTodos(userId).collect { list ->
                _rawTodos.value = list
                updateFilteredList()
            }
        }
    }

    fun updateFilteredList() {
        _filteredTodos.value = _rawTodos.value
            .filter { it.title.contains(searchQuery.value, ignoreCase = true) }
            .filter { selectedFilter.value == null || it.priority == selectedFilter.value?.name }
            .sortedByDescending { Priority.fromString(it.priority).level }
    }

    fun add(userId: String, title: String, priority: String) = viewModelScope.launch {
        try {
            repository.addTodo(userId, title, priority)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggle(userId: String, todo: Todo) = viewModelScope.launch {
        repository.updateTodoStatus(userId, todo.id, !todo.isCompleted)
    }

    fun update(userId: String, todoId: String, title: String, priority: String) = viewModelScope.launch {
        repository.updateTodo(userId, todoId, title, priority)
    }

    fun delete(userId: String, todoId: String) = viewModelScope.launch {
        repository.deleteTodo(userId, todoId)
    }
}