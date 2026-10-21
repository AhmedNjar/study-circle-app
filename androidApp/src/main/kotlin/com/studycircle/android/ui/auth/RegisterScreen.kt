package com.studycircle.android.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.studycircle.android.ui.mvi.rememberMviViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studycircle.android.ui.components.AppMark
import com.studycircle.android.ui.components.DotsDivider
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.usecase.RegisterUseCase
import com.studycircle.shared.features.auth.presentation.register.RegisterEffect
import com.studycircle.shared.features.auth.presentation.register.RegisterIntent
import com.studycircle.shared.features.auth.presentation.register.RegisterViewModel
import com.studycircle.shared.features.auth.presentation.register.RegisterState
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onGoToLogin: () -> Unit) {
    val registerUseCase: RegisterUseCase = koinInject()
    val viewModel = rememberMviViewModel { scope -> RegisterViewModel(registerUseCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                RegisterEffect.NavigateToCircleList -> onRegistered()
            }
        }
    }

    StudyCircleTheme {
        RegisterContent(
            state = state,
            onIntent = { viewModel.dispatch(it) },
            onGoToLogin = onGoToLogin
        )
    }
}

@Composable
fun RegisterContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onGoToLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IslamicBackground(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppMark(subtitle = "حلقات القراءة والتلاوة")
            DotsDivider()
            Spacer(Modifier.height(12.dp))

            PaperCard(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Column(Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = { onIntent(RegisterIntent.DisplayNameChanged(it)) },
                        label = { Text("الاسم") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
                        label = { Text("اسم المستخدم") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "الدور",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = state.role == Role.SUPERVISOR,
                            onClick = { onIntent(RegisterIntent.RoleChanged(Role.SUPERVISOR)) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text("المشرف", fontWeight = FontWeight.Medium) }
                        SegmentedButton(
                            selected = state.role == Role.STUDENT,
                            onClick = { onIntent(RegisterIntent.RoleChanged(Role.STUDENT)) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text("السالك", fontWeight = FontWeight.Medium) }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    state.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(24.dp))
                    if (state.isLoading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        PrimaryButton(text = "إنشاء حساب", onClick = { onIntent(RegisterIntent.Submit) })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "لديك حساب بالفعل؟ ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "تسجيل الدخول",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onGoToLogin() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterContentPreview() {
    StudyCircleTheme {
        RegisterContent(
            state = RegisterState(),
            onIntent = {},
            onGoToLogin = {}
        )
    }
}
