package com.studycircle.android.ui.circle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.studycircle.android.ui.mvi.rememberMviViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.NavTab
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.StudyCircleBottomBar
import com.studycircle.shared.features.circle.domain.usecase.JoinCircleUseCase
import com.studycircle.shared.features.circle.presentation.join.JoinCircleEffect
import com.studycircle.shared.features.circle.presentation.join.JoinCircleIntent
import com.studycircle.shared.features.circle.presentation.join.JoinCircleState
import com.studycircle.shared.features.circle.presentation.join.JoinCircleViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun JoinCircleScreen(
    onJoined: (circleId: String) -> Unit,
    onBack: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {}
) {
    val useCase: JoinCircleUseCase = koinInject()
    val viewModel = rememberMviViewModel { scope -> JoinCircleViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is JoinCircleEffect.NavigateToDetail -> onJoined(effect.circleId)
            }
        }
    }

    JoinCircleContent(
        state = state,
        onIntent = { viewModel.dispatch(it) },
        onBack = onBack,
        onNavigateTab = onNavigateTab
    )
}

@Composable
fun JoinCircleContent(
    state: JoinCircleState,
    onIntent: (JoinCircleIntent) -> Unit,
    onBack: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf("متعلم") }
    var roleExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        IslamicBackground(Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
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
                // Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0A4232),
                                    Color(0xFF0D5840)
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp))

                            Text(
                                "المحفظ والمتعلم",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )

                            // Back Chevron Button on Right
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(size.width * 0.35f, size.height * 0.15f),
                                        end = Offset(size.width * 0.7f, size.height * 0.5f),
                                        strokeWidth = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(size.width * 0.7f, size.height * 0.5f),
                                        end = Offset(size.width * 0.35f, size.height * 0.85f),
                                        strokeWidth = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "الانضمام إلى حلقة",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "خطوة أقرب إلى صحبة القرآن",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Info Banner Card
                    item {
                        PaperCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "معًا نثبت الحفظ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "اطلب رمز الدعوة من محفظك، ثم أدخله للانضمام إلى حلقتك.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.End
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(24.dp)) {
                                        drawCircle(
                                            color = Color(0xFF0D5840),
                                            radius = size.width * 0.25f,
                                            center = Offset(size.width * 0.5f, size.height * 0.35f)
                                        )
                                        val p = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(size.width * 0.2f, size.height * 0.85f)
                                            cubicTo(
                                                size.width * 0.2f, size.height * 0.62f,
                                                size.width * 0.8f, size.height * 0.62f,
                                                size.width * 0.8f, size.height * 0.85f
                                            )
                                        }
                                        drawPath(p, color = Color(0xFF0D5840))
                                    }
                                }
                            }
                        }
                    }

                    // Main Form Card
                    item {
                        PaperCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "رمز الحلقة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.code,
                                    onValueChange = { onIntent(JoinCircleIntent.CodeChanged(it)) },
                                    placeholder = {
                                        Text(
                                            "HL-123456",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            color = Color.Gray
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "ستجد الرمز في الدعوة المرسلة من محفظ الحلقة.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "الانضمام بصفة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedRole,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { roleExpanded = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = roleExpanded,
                                        onDismissRequest = { roleExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("متعلم") },
                                            onClick = {
                                                selectedRole = "متعلم"
                                                roleExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("محفظ") },
                                            onClick = {
                                                selectedRole = "محفظ"
                                                roleExpanded = false
                                            }
                                        )
                                    }
                                }

                                state.errorMessage?.let {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                if (state.isLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Button(
                                        onClick = { onIntent(JoinCircleIntent.Submit) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            "الانضمام إلى الحلقة",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Footer Note
                    item {
                        Text(
                            text = "نسخة تجريبية: تُحفظ الحلقة داخل جلسة التطبيق فقط.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinCircleContentPreview() {
    MaterialTheme {
        JoinCircleContent(
            state = JoinCircleState(),
            onIntent = {}
        )
    }
}
