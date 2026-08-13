package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SecondaryEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    onAddTask: (title: String, description: String, priority: TaskPriority) -> Unit,
    onUpdateTaskStatus: (TaskItem, TaskStatus) -> Unit,
    onDeleteTask: (TaskItem) -> Unit
) {
    var statusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, statusFilter) {
        tasks.filter { statusFilter == null || it.status == statusFilter }
    }

    val completedCount = remember(tasks) { tasks.count { it.status == TaskStatus.DONE } }
    val totalCount = tasks.size
    val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Task Progress Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_progress_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Project Completion Status",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount / $totalCount Done",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { completionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SecondaryEmerald,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("All Tasks ($totalCount)") }
                    )
                }
                items(TaskStatus.values()) { status ->
                    val count = tasks.count { it.status == status }
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = { Text("${status.name.replace("_", " ")} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = "No Tasks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks in this category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onStatusChange = { newStatus -> onUpdateTaskStatus(task, newStatus) },
                            onDelete = { onDeleteTask(task) }
                        )
                    }
                }
            }
        }

        // Add Task FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_task_fab"),
            containerColor = PrimaryCyan,
            contentColor = MaterialTheme.colorScheme.background
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Task")
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, priority ->
                onAddTask(title, desc, priority)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemRow(
    task: TaskItem,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    val (priorityColor, priorityText) = when (task.priority) {
        TaskPriority.URGENT -> MaterialTheme.colorScheme.error to "URGENT"
        TaskPriority.HIGH -> AccentAmber to "HIGH"
        TaskPriority.MEDIUM -> PrimaryCyan to "MEDIUM"
        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant to "LOW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_row_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Checkbox / Icon Button
            IconButton(
                onClick = {
                    val nextStatus = when (task.status) {
                        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                        TaskStatus.IN_PROGRESS -> TaskStatus.DONE
                        TaskStatus.DONE -> TaskStatus.TODO
                    }
                    onStatusChange(nextStatus)
                }
            ) {
                Icon(
                    imageVector = when (task.status) {
                        TaskStatus.DONE -> Icons.Default.CheckCircle
                        TaskStatus.IN_PROGRESS -> Icons.Default.HourglassTop
                        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = task.status.name,
                    tint = if (task.status == TaskStatus.DONE) SecondaryEmerald else PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = priorityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = priorityText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Workspace Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.values().forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, priority)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
