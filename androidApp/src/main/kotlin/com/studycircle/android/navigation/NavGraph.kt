package com.studycircle.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studycircle.android.ui.auth.LoginScreen
import com.studycircle.android.ui.auth.RegisterScreen
import com.studycircle.android.ui.circle.CircleDetailScreen
import com.studycircle.android.ui.circle.CircleHomeScreen
import com.studycircle.android.ui.circle.CreateCircleScreen
import com.studycircle.android.ui.circle.JoinCircleScreen
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.task.CreateTaskScreen
import com.studycircle.android.ui.task.ReviewQueueScreen
import com.studycircle.android.ui.task.TaskListScreen
import com.studycircle.android.ui.task.TaskRunnerScreen
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import com.studycircle.shared.features.auth.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CIRCLE_HOME = "circle_home"
    const val CIRCLE_CREATE = "circle_create"
    const val CIRCLE_JOIN = "circle_join"
    const val CIRCLE_DETAIL = "circle_detail/{circleId}"
    const val TASK_LIST = "circles/{circleId}/tasks"
    const val TASK_CREATE = "circles/{circleId}/tasks/new"
    const val TASK_RUNNER = "tasks/{taskId}/run"
    const val TASK_REVIEW = "tasks/{taskId}/review"

    fun circleDetail(id: String) = "circle_detail/$id"
    fun taskList(circleId: String) = "circles/$circleId/tasks"
    fun taskCreate(circleId: String) = "circles/$circleId/tasks/new"
    fun taskRunner(taskId: String) = "tasks/$taskId/run"
    fun taskReview(taskId: String) = "tasks/$taskId/review"
}

// Whether anyone is logged in, resolved from the persisted session. Navigation is
// derived from this (not from "the login screen happened to call onLoggedIn"), so:
//  - a returning user opens straight to Home instead of seeing Login every launch,
//  - logging out from anywhere sends the user back to Login and clears the back stack.
private sealed interface SessionGate {
    data object Loading : SessionGate
    data object SignedOut : SessionGate
    data class SignedIn(val role: Role) : SessionGate
}

@Composable
fun StudyCircleNavGraph() {
    val observeSession: ObserveSessionUseCase = koinInject()
    val gate by remember {
        observeSession().map<Session?, SessionGate> { session ->
            if (session == null) SessionGate.SignedOut else SessionGate.SignedIn(session.role)
        }
    }.collectAsState(initial = SessionGate.Loading)

    when (val current = gate) {
        // One local-DB read — brief. Show the app background rather than flashing
        // the login screen at someone who is already signed in.
        SessionGate.Loading -> Box(Modifier.fillMaxSize()) { IslamicBackground(Modifier.fillMaxSize()) }
        else -> {
            // Fixed at first resolution: NavHost reads its start destination once.
            val startDestination = remember {
                if (current is SessionGate.SignedIn) Routes.CIRCLE_HOME else Routes.LOGIN
            }
            AppNavHost(startDestination = startDestination, gate = current)
        }
    }
}

@Composable
private fun AppNavHost(startDestination: String, gate: SessionGate) {
    val navController = rememberNavController()
    val role = (gate as? SessionGate.SignedIn)?.role

    // Logged out while somewhere inside the app (logout menu, or a future
    // expired-token handler) → back to Login with a clean back stack.
    LaunchedEffect(gate) {
        if (gate is SessionGate.SignedOut) {
            val route = navController.currentBackStackEntry?.destination?.route
            if (route != null && route != Routes.LOGIN && route != Routes.REGISTER) {
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    // Bottom-bar tabs. The tasks list and the profile/logout menu both live on Home
    // (tasks section / header menu), so those two tabs land there; "الحلقات" goes to
    // the one circle action the user's role is allowed to perform.
    fun handleNavigateTab(tabId: String) {
        when (tabId) {
            "home", "tasks", "profile" -> navController.navigate(Routes.CIRCLE_HOME) {
                popUpTo(Routes.CIRCLE_HOME) { inclusive = false }
                launchSingleTop = true
            }
            "circles" -> navController.navigate(
                if (role == Role.SUPERVISOR) Routes.CIRCLE_CREATE else Routes.CIRCLE_JOIN
            ) { launchSingleTop = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.CIRCLE_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.CIRCLE_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.CIRCLE_HOME) {
            CircleHomeScreen(
                onCreateCircle = { navController.navigate(Routes.CIRCLE_CREATE) },
                onJoinCircle = { navController.navigate(Routes.CIRCLE_JOIN) },
                onOpenCircle = { circleId -> navController.navigate(Routes.circleDetail(circleId)) },
                onOpenTask = { taskId -> navController.navigate(Routes.taskRunner(taskId)) },
                onReviewTask = { taskId -> navController.navigate(Routes.taskReview(taskId)) },
                onNavigateTab = ::handleNavigateTab
            )
        }
        composable(Routes.CIRCLE_CREATE) {
            CreateCircleScreen(
                // Replace the form with the new circle so Back doesn't return to a
                // stale, already-submitted "create" screen.
                onCreated = { id ->
                    navController.navigate(Routes.circleDetail(id)) {
                        popUpTo(Routes.CIRCLE_CREATE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateTab = ::handleNavigateTab
            )
        }
        composable(Routes.CIRCLE_JOIN) {
            JoinCircleScreen(
                onJoined = { id ->
                    navController.navigate(Routes.circleDetail(id)) {
                        popUpTo(Routes.CIRCLE_JOIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateTab = ::handleNavigateTab
            )
        }
        composable(
            Routes.CIRCLE_DETAIL,
            arguments = listOf(navArgument("circleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val circleId = backStackEntry.arguments?.getString("circleId") ?: return@composable
            CircleDetailScreen(circleId = circleId, onViewTasks = { navController.navigate(Routes.taskList(circleId)) })
        }
        composable(
            Routes.TASK_LIST,
            arguments = listOf(navArgument("circleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val circleId = backStackEntry.arguments?.getString("circleId") ?: return@composable
            TaskListScreen(
                circleId = circleId,
                isSupervisor = role == Role.SUPERVISOR,
                onCreateTask = { navController.navigate(Routes.taskCreate(circleId)) },
                onOpenTask = { taskId -> navController.navigate(Routes.taskRunner(taskId)) },
                onReviewTask = { taskId -> navController.navigate(Routes.taskReview(taskId)) }
            )
        }
        composable(
            Routes.TASK_CREATE,
            arguments = listOf(navArgument("circleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val circleId = backStackEntry.arguments?.getString("circleId") ?: return@composable
            CreateTaskScreen(
                circleId = circleId,
                onCreated = { navController.popBackStack() }
            )
        }
        composable(
            Routes.TASK_RUNNER,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskRunnerScreen(
                taskId = taskId,
                onSubmitted = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onNavigateTab = ::handleNavigateTab
            )
        }
        composable(
            Routes.TASK_REVIEW,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            ReviewQueueScreen(taskId = taskId)
        }
    }
}
