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

import androidx.compose.ui.tooling.preview.Preview
import com.studycircle.shared.features.auth.presentation.register.RegisterState

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
        onIntent = { viewModel.dispatch(it) },
        onGoToLogin = onGoToLogin
    )
}

@Composable
fun RegisterContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
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
            onValueChange = { onIntent(RegisterIntent.DisplayNameChanged(it)) },
            label = { Text("الاسم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
            label = { Text("اسم المستخدم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        Text("الدور", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        // Directly exercises the Role enum + ArabicTerms mapping from step 1 —
        // this toggle is the same one shown in the earlier UI/UX mockup.
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.role == Role.SUPERVISOR,
                onClick = { onIntent(RegisterIntent.RoleChanged(Role.SUPERVISOR)) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("المشرف") }
            SegmentedButton(
                selected = state.role == Role.STUDENT,
                onClick = { onIntent(RegisterIntent.RoleChanged(Role.STUDENT)) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("السالك") }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
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
            PrimaryButton(text = "إنشاء حساب", onClick = { onIntent(RegisterIntent.Submit) })
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

@Preview(showBackground = true)
@Composable
fun RegisterContentPreview() {
    MaterialTheme {
        RegisterContent(
            state = RegisterState(),
            onIntent = {},
            onGoToLogin = {}
        )
    }
}
