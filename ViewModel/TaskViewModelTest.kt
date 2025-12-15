package com.example.todolist.ViewModel

import com.example.todolist.Database.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TaskViewModel
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        taskDao = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when a new task is added it has a non-empty name`() = runTest {

        `when`(taskDao.getAllTasks()).thenReturn(flowOf(emptyList()))
        viewModel = TaskViewModel(taskDao)
        val nonEmptyTitle = "New Task"

        viewModel.addTask(nonEmptyTitle)
        advanceUntilIdle()

        val taskCaptor = argumentCaptor<Task>()
        verify(taskDao).insert(taskCaptor.capture())

        val capturedTask = taskCaptor.firstValue
        assertTrue(capturedTask.title.isNotEmpty())
        assertEquals(nonEmptyTitle, capturedTask.title)
    }

    @Test
    fun `when a task title is updated with a non-empty string the DAO is called`() = runTest {

        val initialTask = Task(id = 1, title = "Original Title")
        val newTitle = "Updated Title"
        `when`(taskDao.getAllTasks()).thenReturn(flowOf(listOf(initialTask)))
        viewModel = TaskViewModel(taskDao)
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateTaskTitle(initialTask.id, newTitle)
        advanceUntilIdle()

        val taskCaptor = argumentCaptor<Task>()
        verify(taskDao).update(taskCaptor.capture())
        assertEquals(newTitle, taskCaptor.firstValue.title)
    }

    @Test
    fun `when a task is toggled the DAO is called with the updated state`() = runTest {

        val initialTask = Task(id = 1, title = "A Task", isDone = false)
        `when`(taskDao.getAllTasks()).thenReturn(flowOf(listOf(initialTask)))
        viewModel = TaskViewModel(taskDao)
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleTaskDone(initialTask.id)
        advanceUntilIdle()

        val taskCaptor = argumentCaptor<Task>()
        verify(taskDao).update(taskCaptor.capture())

        val updatedTask = taskCaptor.firstValue
        assertTrue(updatedTask.isDone)
    }

    @Test
    fun `when task title is updated with an empty string the DAO is not called`() = runTest {

        val task = Task(id = 1, title = "Original Title")
        `when`(taskDao.getAllTasks()).thenReturn(flowOf(listOf(task)))
        viewModel = TaskViewModel(taskDao)
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateTaskTitle(task.id, "   ")
        advanceUntilIdle()

        verify(taskDao, Mockito.never()).update(any())
    }

    @Test
    fun `when all tasks are deleted the DAO is called`() = runTest {

        `when`(taskDao.getAllTasks()).thenReturn(flowOf(listOf(Task(id = 1, title = "A Task"))))
        viewModel = TaskViewModel(taskDao)
        viewModel.uiState.first { !it.isLoading }

        viewModel.deleteAllTasks()
        advanceUntilIdle()

        verify(taskDao).deleteAllTasks()
    }

    @Test
    fun `when a task is deleted the DAO is called`() = runTest {

        val taskToDelete = Task(id = 1, title = "A Task to Delete")
        `when`(taskDao.getAllTasks()).thenReturn(flowOf(listOf(taskToDelete)))
        viewModel = TaskViewModel(taskDao)
        viewModel.uiState.first { !it.isLoading }

        viewModel.deleteTask(taskToDelete.id)
        advanceUntilIdle()

        val taskCaptor = argumentCaptor<Task>()
        verify(taskDao).delete(taskCaptor.capture())
        assertEquals(
            taskToDelete,
            taskCaptor.firstValue
        )
    }
}