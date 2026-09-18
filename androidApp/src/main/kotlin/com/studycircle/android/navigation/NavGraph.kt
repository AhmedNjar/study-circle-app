package com.studycircle.android.navigation

import androidx.compose.runtime.Composable
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
import com.studycircle.android.ui.task.CreateTaskScreen
import com.studycircle.android.ui.task.ReviewQueueScreen
import com.studycircle.android.ui.task.TaskListScreen
import com.studycircle.android.ui.task.TaskRunnerScreen

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

@Composable
fun StudyCircleNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
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
                onJoinCircle = { navController.navigate(Routes.CIRCLE_JOIN) }
            )
        }
        composable(Routes.CIRCLE_CREATE) {
            CreateCircleScreen(onCreated = { id -> navController.navigate(Routes.circleDetail(id)) })
        }
        composable(Routes.CIRCLE_JOIN) {
            JoinCircleScreen(onJoined = { id -> navController.navigate(Routes.circleDetail(id)) })
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
                onCreated = { navController.popBackStack() } // back to the (now-refreshable) task list
            )
        }
        composable(
            Routes.TASK_RUNNER,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskRunnerScreen(taskId = taskId, onSubmitted = { navController.popBackStack() })
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
