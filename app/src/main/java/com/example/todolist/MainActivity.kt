package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.todolist.Database.TaskDatabase
import com.example.todolist.Screens.AppNavigation
import com.example.todolist.ViewModel.TaskViewModel
import com.example.todolist.ViewModel.TaskViewModelFactory
import com.example.todolist.ui.theme.TodolistTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { TaskDatabase.getDatabase(this) }

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(database.taskDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TodolistTheme {
                AppNavigation(viewModel)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("is_app_running", true)
        super.onSaveInstanceState(outState)
    }
}