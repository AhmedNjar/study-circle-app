package com.studycircle.android.ui.circle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.NavTab
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.SectionTitle
import com.studycircle.android.ui.components.StudyCircleBottomBar
import com.studycircle.android.ui.components.activityEmoji
import com.studycircle.android.ui.components.formatTimeAgo
import com.studycircle.android.ui.components.initialsOf
import com.studycircle.android.ui.components.startLabel
import com.studycircle.android.ui.components.toArabicDigits
import com.studycircle.android.ui.components.toArabicLong
import com.studycircle.android.ui.components.trackingSummary
import com.studycircle.android.ui.mvi.rememberMviViewModel
import com.studycircle.android.ui.theme.LocalThemeController
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.auth.domain.usecase.LogoutUseCase
import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.home.domain.model.HomeTask
import com.studycircle.shared.features.home.domain.model.PendingReview
import com.studycircle.shared.features.home.domain.usecase.GetHomeDashboardUseCase
import com.studycircle.shared.features.home.presentation.HomeEffect
import com.studycircle.shared.features.home.presentation.HomeIntent
import com.studycircle.shared.features.home.presentation.HomeState
import com.studycircle.shared.features.home.presentation.HomeViewModel
import com.studycircle.shared.features.home.presentation.ReviewDecision
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import com.studycircle.shared.features.task.domain.usecase.SyncPendingSubmissionsUseCase
import com.studycircle.shared.localization.ArabicTerms
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

private val Emerald = Color(0xFF0A4232)
private val EmeraldLight = Color(0xFF0D5840)
private val Approve = Color(0xFF22C55E)
private val Reject = Color(0xFFEF4444)
private val AvatarColors = listOf(Color(0xFF0D5840), Color(0xFFC9A227), Color(0xFF1B6B8A), Color(0xFF7A3E9D), Color(0xFFB4532A))

// How many active tasks the dashboard lists before pointing at the circle's own
// task list.
private const val MAX_DASHBOARD_TASKS = 5

@Composable
fun CircleHomeScreen(
    onCreateCircle: () -> Unit,
    onJoinCircle: () -> Unit,
    onOpenCircle: (circleId: String) -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onReviewTask: (taskId: String) -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    val getHomeDashboard: GetHomeDashboardUseCase = koinInject()
    val reviewSubmission: ReviewSubmissionUseCase = koinInject()
    val syncPending: SyncPendingSubmissionsUseCase = koinInject()
    val logout: LogoutUseCase = koinInject()
    val viewModel = rememberMviViewModel { scope ->
        HomeViewModel(getHomeDashboard, reviewSubmission, syncPending, logout, scope)
    }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reload whenever the screen comes back into view (after creating a circle,
    // submitting a task, ...). The Home back-stack entry — and so its ViewModel —
    // stays alive underneath those screens, so without this it would show stale data.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.dispatch(HomeIntent.Load) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    StudyCircleTheme {
        CircleHomeContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::dispatch,
            onCreateCircle = onCreateCircle,
            onJoinCircle = onJoinCircle,
            onOpenCircle = onOpenCircle,
            onOpenTask = onOpenTask,
            onReviewTask = onReviewTask,
            onNavigateTab = onNavigateTab
        )
    }
}

@Composable
fun CircleHomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onCreateCircle: () -> Unit,
    onJoinCircle: () -> Unit,
    onOpenCircle: (String) -> Unit = {},
    onOpenTask: (String) -> Unit = {},
    onReviewTask: (String) -> Unit = {},
    onNavigateTab: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val themeController = LocalThemeController.current
    val dashboard = state.dashboard
    val isSupervisor = dashboard?.isSupervisor == true
    var profileMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        IslamicBackground(Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                StudyCircleBottomBar(
                    currentTab = NavTab.HOME,
                    onTabSelected = { tab -> onNavigateTab(tab.id) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(Emerald, EmeraldLight)))
                        .statusBarsPadding()
                        .padding(bottom = 24.dp)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dark-mode toggle (light → dark → follow system)
                            IconButton(
                                onClick = { themeController.onToggle() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val isDark = themeController.isDark == true
                                    if (isDark) {
                                        drawCircle(Color(0xFFD4AF37), radius = size.width * 0.45f)
                                        drawCircle(Emerald, radius = size.width * 0.35f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                                    } else {
                                        drawCircle(Color(0xFFD4AF37), radius = size.width * 0.35f)
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "المحفظ والمتعلم",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "حلقات القراءة والتلاوة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                                )
                            }

                            // Profile avatar → menu (logout)
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                        .clickable { profileMenuOpen = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dashboard?.session?.displayName?.let(::initialsOf)?.take(1) ?: "؟",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Emerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                DropdownMenu(
                                    expanded = profileMenuOpen,
                                    onDismissRequest = { profileMenuOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("تسجيل الخروج") },
                                        onClick = {
                                            profileMenuOpen = false
                                            onIntent(HomeIntent.Logout)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "أهلاً بك،",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = dashboard?.session?.displayName?.let { "$it ✦" } ?: " ",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            dashboard?.let {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        ArabicTerms.roleLabel(it.session.role),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Body
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (state.isLoading && dashboard == null) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    state.errorMessage?.let { message ->
                        item { ErrorBanner(message = message, onRetry = { onIntent(HomeIntent.Refresh) }) }
                    }
                    if (dashboard?.isPartial == true && state.errorMessage == null) {
                        item {
                            ErrorBanner(
                                message = "تعذر تحميل بعض البيانات. المعروض قد يكون ناقصًا.",
                                onRetry = { onIntent(HomeIntent.Refresh) }
                            )
                        }
                    }

                    if (dashboard != null) {
                        // Quick action — only the one the role can actually use
                        // (the backend rejects create-circle for students and
                        // join-circle for supervisors).
                        item {
                            SectionTitle(title = "الإجراءات السريعة")
                            Spacer(Modifier.height(12.dp))
                            if (isSupervisor) {
                                QuickActionCard(
                                    title = "إنشاء حلقة جديدة",
                                    buttonText = "ابدأ",
                                    onClick = onCreateCircle,
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                QuickActionCard(
                                    title = "الانضمام إلى حلقة بالرمز",
                                    buttonText = "ابدأ",
                                    onClick = onJoinCircle,
                                    isPrimary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // My circles
                        item {
                            SectionTitle(title = "حلقاتي")
                            Spacer(Modifier.height(12.dp))
                            if (dashboard.circles.isEmpty()) {
                                EmptyHint(
                                    if (isSupervisor) "لم تنشئ أي حلقة بعد. أنشئ حلقتك الأولى وشارك رمزها مع الطلاب."
                                    else "لم تنضم إلى أي حلقة بعد. اطلب رمز الانضمام من مشرفك."
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    dashboard.circles.forEach { circle ->
                                        CircleRowCard(
                                            circle = circle,
                                            showJoinCode = isSupervisor,
                                            onClick = { onOpenCircle(circle.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Active tasks
                        item {
                            SectionTitle(title = "المهام النشطة")
                            Spacer(Modifier.height(12.dp))
                            if (dashboard.activeTasks.isEmpty()) {
                                EmptyHint("لا توجد مهام نشطة الآن.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    dashboard.activeTasks.take(MAX_DASHBOARD_TASKS).forEach { homeTask ->
                                        DashboardTaskCard(
                                            item = homeTask,
                                            isSupervisor = isSupervisor,
                                            onClick = {
                                                if (isSupervisor) onReviewTask(homeTask.task.id) else onOpenTask(homeTask.task.id)
                                            }
                                        )
                                    }
                                    val hidden = dashboard.activeTasks.size - MAX_DASHBOARD_TASKS
                                    if (hidden > 0) {
                                        Text(
                                            "و ${hidden.toArabicDigits()} مهام أخرى في صفحات الحلقات",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }

                        // Pending reviews (supervisor only)
                        if (isSupervisor) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(24.dp).clip(CircleShape).background(Reject),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = state.pendingReviewCount.toArabicDigits(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }
                                        Text(
                                            text = "المراجعات المعلقة",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                                            )
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    if (dashboard.pendingReviews.isEmpty()) {
                                        EmptyHint("لا توجد تسليمات بانتظار المراجعة. بارك الله فيك.")
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            dashboard.pendingReviews.forEach { review ->
                                                PendingReviewCard(
                                                    review = review,
                                                    decision = state.decisions[review.submissionId],
                                                    inFlight = review.submissionId in state.reviewsInFlight,
                                                    onApprove = { onIntent(HomeIntent.Approve(review.submissionId)) },
                                                    onReject = { onIntent(HomeIntent.Reject(review.submissionId)) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Summary widget
                        item {
                            if (isSupervisor) {
                                SummaryWidget(
                                    title = "نظرة على حلقاتك",
                                    subtitle = "متابعة ما يحتاج إلى انتباهك",
                                    progress = null,
                                    stats = listOf(
                                        Stat(dashboard.circles.size.toArabicDigits(), "حلقة", false),
                                        Stat(dashboard.activeTasks.size.toArabicDigits(), "مهمة نشطة", false),
                                        Stat(state.pendingReviewCount.toArabicDigits(), "بانتظار المراجعة", true)
                                    )
                                )
                            } else {
                                SummaryWidget(
                                    title = "رباطك",
                                    subtitle = "أسبوع متواصل هو أول معالمك",
                                    // Progress toward a first full week of consecutive days.
                                    progress = (dashboard.currentStreak.coerceAtMost(7)) / 7f,
                                    stats = listOf(
                                        Stat(dashboard.activeTasks.size.toArabicDigits(), "مهمة نشطة", false),
                                        Stat(dashboard.bestStreak.toArabicDigits(), "أفضل رباط", false),
                                        Stat(dashboard.currentStreak.toArabicDigits(), "أيام متتالية", true)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    PaperCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("إعادة المحاولة", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CircleRowCard(circle: Circle, showJoinCode: Boolean, onClick: () -> Unit) {
    PaperCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("☪", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary) }

            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    circle.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${circle.memberIds.size.toArabicDigits()} ${if (circle.memberIds.size == 1) "عضو" else "أعضاء"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (showJoinCode) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        circle.joinCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Wording for what a submission contains, built from evidence — never from guesses
// about the task's title.
private fun reviewDescription(review: PendingReview): String {
    val what = when (val e = review.evidence) {
        is SubmissionEvidence.TimedReflection -> "أنهى جلسة «${review.taskTitle}»"
        is SubmissionEvidence.Media -> "سجّل «${review.taskTitle}»"
        is SubmissionEvidence.Count -> "أنجز ${e.countAchieved.toArabicDigits()} في «${review.taskTitle}»"
        SubmissionEvidence.CheckIn -> "أكّد إنجاز «${review.taskTitle}»"
    }
    return if (review.onBehalfOfGroup) "$what (ضمن مجموعة)" else what
}

@Composable
private fun PendingReviewCard(
    review: PendingReview,
    decision: ReviewDecision?,
    inFlight: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    when (decision) {
        null -> PaperCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.alpha(if (inFlight) 0.4f else 1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Reject)
                            .clickable(enabled = !inFlight) { onReject() },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), 2.5.dp.toPx(), StrokeCap.Round)
                            drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), 2.5.dp.toPx(), StrokeCap.Round)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Approve)
                            .clickable(enabled = !inFlight) { onApprove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(14.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.15f, size.height * 0.5f)
                                lineTo(size.width * 0.42f, size.height * 0.78f)
                                lineTo(size.width * 0.88f, size.height * 0.22f)
                            }
                            drawPath(path, Color.White, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = review.submitterName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = reviewDescription(review),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.End
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${review.circleName} · ${formatTimeAgo(review.submittedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AvatarColors[(review.submitterName.hashCode() and Int.MAX_VALUE) % AvatarColors.size]),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(review.submitterName),
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        ReviewDecision.APPROVED -> ResolvedRow("✓", Approve, "تم قبول تسليم ${review.submitterName}")
        ReviewDecision.REJECTED -> ResolvedRow("✕", Reject, "تم رفض تسليم ${review.submitterName}")
    }
}

@Composable
private fun ResolvedRow(glyph: String, color: Color, text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(glyph, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    buttonText: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    PaperCard(
        modifier = modifier,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        borderColor = if (isPrimary) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPrimary) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(if (isPrimary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (isPrimary) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isPrimary) MaterialTheme.colorScheme.secondary else Emerald
                )
            ) {
                Text(buttonText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DashboardTaskCard(item: HomeTask, isSupervisor: Boolean, onClick: () -> Unit) {
    val task = item.task
    PaperCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .align(Alignment.CenterEnd)
            )

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${activityEmoji(task.activityType)}  ${task.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Students see their الرِّباط in this circle; a streak of 0 isn't
                    // worth a badge, so fall back to the circle's name.
                    val tag = if (!isSupervisor && item.streakCount > 0) {
                        "${ArabicTerms.STREAK_LABEL} ${item.streakCount.toArabicDigits()}"
                    } else {
                        item.circleName
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "تاريخ الاستحقاق: ${task.dueDate.toArabicLong()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trackingSummary(task.tracking),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    TextButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isSupervisor) "مراجعة التسليمات" else startLabel(task.activityType),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class Stat(val value: String, val label: String, val isAccent: Boolean)

@Composable
private fun SummaryWidget(title: String, subtitle: String, progress: Float?, stats: List<Stat>) {
    PaperCard(modifier = Modifier.fillMaxWidth(), color = Emerald) {
        Box {
            IslamicBackground(Modifier.fillMaxSize().alpha(0.2f))

            Column(Modifier.padding(20.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                )
                if (progress != null) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                        Text(
                            "${(progress * 100).toInt().toArabicDigits()}٪",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    stats.forEach { ProgressStat(it.value, it.label, it.isAccent) }
                }
            }
        }
    }
}

@Composable
private fun ProgressStat(value: String, label: String, isAccent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = if (isAccent) MaterialTheme.colorScheme.secondary else Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
    }
}

@Preview(showBackground = true)
@Composable
fun CircleHomeLoadingPreview() {
    StudyCircleTheme {
        CircleHomeContent(
            state = HomeState(isLoading = true),
            onIntent = {},
            onCreateCircle = {},
            onJoinCircle = {}
        )
    }
}
