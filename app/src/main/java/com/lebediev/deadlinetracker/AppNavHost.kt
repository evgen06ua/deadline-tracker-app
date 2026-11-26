package com.lebediev.deadlinetracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lebediev.deadlinetracker.ui.DeadlineEditScreen
import com.lebediev.deadlinetracker.ui.DeadlineEditViewModel
import com.lebediev.deadlinetracker.ui.DeadlineListScreen
import com.lebediev.deadlinetracker.ui.DeadlineListViewModel

@Composable
fun AppNavHost(vm: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by vm.isLoggedIn.collectAsState()
    val startDestination = remember { if (vm.isLoggedIn.value) "home" else "login" }

    LaunchedEffect(isLoggedIn) {
        val current = navController.currentDestination?.route
        if (isLoggedIn && current != "home") {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isLoggedIn && current != "login" && current != "register") {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(vm) {
                vm.clearForm()
                navController.navigate("register")
            }
        }
        composable("register") {
            RegisterScreen(vm) {
                vm.clearForm()
                navController.popBackStack()
            }
        }
        composable("home") {
            val listVm: DeadlineListViewModel = viewModel()
            DeadlineListScreen(
                vm = listVm,
                onAdd = { navController.navigate("edit") },
                onOpen = { id -> navController.navigate("edit?id=$id") },
                onLogout = { vm.logout() }
            )
        }
        composable(
            route = "edit?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id")
            val editVm: DeadlineEditViewModel = viewModel(key = id ?: "new") {
                DeadlineEditViewModel(id)
            }
            DeadlineEditScreen(
                vm = editVm,
                isEdit = id != null,
                onDone = { navController.popBackStack() }
            )
        }
    }
}