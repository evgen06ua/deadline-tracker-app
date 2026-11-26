package com.lebediev.deadlinetracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebediev.deadlinetracker.validation.DeadlineValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineEditScreen(vm: DeadlineEditViewModel, isEdit: Boolean, onDone: () -> Unit) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit deadline" else "New deadline") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Back") } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = vm::onTitle,
                    label = { Text("Title") },
                    isError = state.errors.title != null,
                    supportingText = { state.errors.title?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Text("Course", style = MaterialTheme.typography.titleSmall)
                state.courses.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = c.id == state.courseId,
                            onClick = { vm.onCourse(c) }
                        )
                        Text(c.name)
                    }
                }
                state.errors.course?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.dueDateText,
                    onValueChange = vm::onDate,
                    label = { Text("Due date (yyyy-mm-dd)") },
                    isError = state.errors.dueDate != null,
                    supportingText = { state.errors.dueDate?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                Text("Priority", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DeadlineValidator.priorities.forEach { p ->
                        RadioButton(
                            selected = p == state.priority,
                            onClick = { vm.onPriority(p) }
                        )
                        Text(p)
                    }
                }
                state.errors.priority?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.done, onCheckedChange = vm::onDone)
                    Text("Done")
                }

                state.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = vm::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.height(20.dp))
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}