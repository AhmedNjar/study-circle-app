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
import com.studycircle.shared.features.auth.domain.usecase.LoginUseCase
import com.studycircle.shared.features.auth.presentation.login.LoginEffect
import com.studycircle.shared.features.auth.presentation.login.LoginIntent
import com.studycircle.shared.features.auth.presentation.login.LoginViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

import androidx.compose.ui.tooling.preview.Preview
import com.studycircle.shared.features.auth.presentation.login.LoginState

// LoginViewModel isn't Koin-scoped to this composable's lifecycle — it's
// built with rememberCoroutineScope() rather than a real viewModelScope.
// That's fine for Phase 1 (screen-local, simple flows); bridging MviViewModel
// to androidx.lifecycle.ViewModel for proper scope survival across
// navigation/rotation is a Phase 2+ polish item, not a Phase 1 blocker.
@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onGoToRegister: () -> Unit) {
    val loginUseCase: LoginUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { LoginViewModel(loginUseCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateToCircleList -> onLoggedIn()
            }
        }
    }

    LoginContent(
        state = state,
        onIntent = { viewModel.dispatch(it) },
        onGoToRegister = onGoToRegister
    )
}

@Composable
fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onGoToRegister: () -> Unit
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
            value = state.username,
            onValueChange = { onIntent(LoginIntent.UsernameChanged(it)) },
            label = { Text("اسم المستخدم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
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
            PrimaryButton(text = "تسجيل الدخول", onClick = { onIntent(LoginIntent.Submit) })
        }

        Spacer(Modifier.height(16.dp))
        Row {
            Text("ليس لديك حساب؟ ")
            Text(
                "إنشاء حساب",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onGoToRegister() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    MaterialTheme {
        LoginContent(
            state = LoginState(),
            onIntent = {},
            onGoToRegister = {}
        )
    }
}
