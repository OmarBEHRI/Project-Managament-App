package com.example.projectmanager.ui.task

import androidx.lifecycle.ViewModel
import com.example.projectmanager.data.model.TaskPriority
import com.example.projectmanager.data.model.TaskStatus
import com.example.projectmanager.ui.tasks.TaskViewModel as TasksTaskViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

/**
 * This is a simple pass-through ViewModel that uses the TaskViewModel from the tasks package.
 * We're removing this class since it's causing type mismatch issues.
 */
@HiltViewModel
class TaskViewModel @Inject constructor() : ViewModel() {
    // This class is not used anymore - we're directly using the TaskViewModel from tasks package
}
