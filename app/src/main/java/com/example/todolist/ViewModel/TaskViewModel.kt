package com.example.todolist.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.Database.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    val uiState: StateFlow<TaskListUiState> =
        taskDao.getAllTasks()
            .map { tasks ->
                TaskListUiState(
                    tasks = tasks,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = TaskListUiState(isLoading = true)
            )

    suspend fun addTask(title: String) {
        withContext(Dispatchers.IO) {
            val newTask = Task(title = title)
            taskDao.insert(newTask)
        }
    }

    fun toggleTaskDone(taskId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val taskToUpdate = uiState.value.tasks.find { it.id == taskId }
                taskToUpdate?.let {
                    val updatedTask = it.copy(isDone = !it.isDone)
                    taskDao.update(updatedTask)
                }
            }
        }
    }

    fun updateTaskTitle(taskId: Int, newTitle: String) {
        if (newTitle.isBlank()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val taskToUpdate = uiState.value.tasks.find { it.id == taskId }

                taskToUpdate?.let {
                    val updatedTask = it.copy(title = newTitle.trim())
                    taskDao.update(updatedTask)
                }
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val taskToDelete = uiState.value.tasks.find { it.id == taskId }
                taskToDelete?.let {
                    taskDao.delete(it)
                }
            }
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                taskDao.deleteAllTasks()
            }
        }
    }
}


class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

