package com.lebediev.deadlinetracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(vm: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("DeadlineTracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Students lose track of deadlines spread across many courses, and DeadlineTracker gives them one clear, always-available overview.")
        Spacer(Modifier.height(16.dp))
        Text("Logged in as ${vm.currentEmail()}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = vm::logout) { Text("Log out") }
    }
}
