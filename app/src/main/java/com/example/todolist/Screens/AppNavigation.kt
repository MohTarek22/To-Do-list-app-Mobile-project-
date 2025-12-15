package com.example.todolist.Screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todolist.ViewModel.TaskViewModel

const val ROUTE_TASKS = "tasks_list"
const val ROUTE_ADD_TASK = "add_task"

@Composable
fun AppNavigation(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = ROUTE_TASKS
    ) {
        composable(ROUTE_TASKS) {
            TaskListScreen(
                tasksState = uiState,
                onToggleDone = viewModel::toggleTaskDone,
                onNavigateToAdd = {
                    navController.navigate(ROUTE_ADD_TASK)
                },
                onDeleteTask = viewModel::deleteTask,
                onDeleteAll = viewModel::deleteAllTasks,
                onUpdateTaskTitle = viewModel::updateTaskTitle
            )
        }
        composable(ROUTE_ADD_TASK) {
            AddTaskScreen(
                onSaveTask = viewModel::addTask,
                onNavigateBack = {
                    navController.popBackStack(ROUTE_TASKS, inclusive = false)
                }
            )
        }
    }
}