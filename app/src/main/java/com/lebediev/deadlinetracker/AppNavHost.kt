package com.lebediev.deadlinetracker

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(vm: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by vm.isLoggedIn.collectAsState()
    val startDestination = remember { if (vm.isLoggedIn.value) "home" else "login" }

    // react to login / logout and clear the back stack
    LaunchedEffect(isLoggedIn) {
        val current = navController.currentDestination?.route
        if (isLoggedIn && current != "home") {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isLoggedIn && current == "home") {
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
        composable("home") { HomeScreen(vm) }
    }
}