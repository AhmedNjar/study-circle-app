package com.studycircle.android.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.AppMark
import com.studycircle.android.ui.components.DotsDivider
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.usecase.RegisterUseCase
import com.studycircle.shared.features.auth.presentation.register.RegisterEffect
import com.studycircle.shared.features.auth.presentation.register.RegisterIntent
import com.studycircle.shared.features.auth.presentation.register.RegisterViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onGoToLogin: () -> Unit) {
    val registerUseCase: RegisterUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { RegisterViewModel(registerUseCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                RegisterEffect.NavigateToCircleList -> onRegistered()
            }
        }
    }

    RegisterContent(
        state = state,
        onDisplayNameChanged = { viewModel.dispatch(RegisterIntent.DisplayNameChanged(it)) },
        onUsernameChanged = { viewModel.dispatch(RegisterIntent.UsernameChanged(it)) },
        onRoleChanged = { viewModel.dispatch(RegisterIntent.RoleChanged(it)) },
        onPasswordChanged = { viewModel.dispatch(RegisterIntent.PasswordChanged(it)) },
        onSubmit = { viewModel.dispatch(RegisterIntent.Submit) },
        onGoToLogin = onGoToLogin
    )
}

@Composable
fun RegisterContent(
    state: com.studycircle.shared.features.auth.presentation.register.RegisterState,
    onDisplayNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onRoleChanged: (Role) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppMark(subtitle = "حلقات القراءة والتلاوة")
        DotsDivider()
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text("الاسم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = { Text("اسم المستخدم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        Text("الدور", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.role == Role.SUPERVISOR,
                onClick = { onRoleChanged(Role.SUPERVISOR) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("المشرف") }
            SegmentedButton(
                selected = state.role == Role.STUDENT,
                onClick = { onRoleChanged(Role.STUDENT) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("السالك") }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("كلمة المرور") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            PrimaryButton(text = "إنشاء حساب", onClick = onSubmit)
        }

        Spacer(Modifier.height(16.dp))
        Row {
            Text("لديك حساب بالفعل؟ ")
            Text(
                "تسجيل الدخول",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onGoToLogin() }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun RegisterPreview() {
    MaterialTheme {
        RegisterContent(
            state = com.studycircle.shared.features.auth.presentation.register.RegisterState(
                displayName = "الاسم",
                username = "user123",
                role = Role.STUDENT
            ),
            onDisplayNameChanged = {},
            onUsernameChanged = {},
            onRoleChanged = {},
            onPasswordChanged = {},
            onSubmit = {},
            onGoToLogin = {}
        )
    }
}
