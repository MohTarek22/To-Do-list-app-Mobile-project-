package com.example.todolist.ViewModel

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)