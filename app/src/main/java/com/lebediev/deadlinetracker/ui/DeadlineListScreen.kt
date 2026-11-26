package com.lebediev.deadlinetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebediev.deadlinetracker.data.model.Deadline
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(date: Date?): String =
    if (date == null) "no date" else SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlineListScreen(
    vm: DeadlineListViewModel,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var toDelete by remember { mutableStateOf<Deadline?>(null) }
    val error = state.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My deadlines") },
                actions = { TextButton(onClick = onLogout) { Text("Log out") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd) { Text("Add") }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!state.isLoading && error == null) {
                Text(
                    "${state.stats.upcoming} upcoming | ${state.stats.overdue} overdue | ${state.stats.done} done",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQuery,
                    label = { Text("Search by title or course") },
                    singleLine = true,
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            TextButton(onClick = { vm.onQuery("") }) { Text("Clear") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (error != null) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else if (state.deadlines.isEmpty()) {
                    Text(
                        if (state.allCount > 0) "No deadlines match your search."
                        else "No deadlines yet. Tap Add to create the first one.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.deadlines, key = { it.id }) { d ->
                            DeadlineRow(
                                d = d,
                                onClick = { onOpen(d.id) },
                                onDelete = { toDelete = d }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    val item = toDelete
    if (item != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete deadline") },
            text = { Text("Delete \"${item.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    vm.delete(item.id) { ok ->
                        scope.launch {
                            snackbar.showSnackbar(if (ok) "Deleted" else "Could not delete")
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DeadlineRow(d: Deadline, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(d.title, style = MaterialTheme.typography.titleMedium)
                Text("Course: ${d.courseName}")
                Text("Due: ${formatDate(d.dueDate)}")
                Text("Priority: ${d.priority}")
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}