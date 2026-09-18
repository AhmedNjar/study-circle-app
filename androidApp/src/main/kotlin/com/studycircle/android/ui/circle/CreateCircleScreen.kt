package com.studycircle.android.ui.circle

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.shared.features.circle.domain.usecase.CreateCircleUseCase
import com.studycircle.shared.features.circle.presentation.create.CreateCircleEffect
import com.studycircle.shared.features.circle.presentation.create.CreateCircleIntent
import com.studycircle.shared.features.circle.presentation.create.CreateCircleViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun CreateCircleScreen(onCreated: (circleId: String) -> Unit) {
    val useCase: CreateCircleUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { CreateCircleViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CreateCircleEffect.NavigateToDetail -> onCreated(effect.circleId)
            }
        }
    }

    CreateCircleContent(
        state = state,
        onNameChanged = { viewModel.dispatch(CreateCircleIntent.NameChanged(it)) },
        onSubmit = { viewModel.dispatch(CreateCircleIntent.Submit) }
    )
}

@Composable
fun CreateCircleContent(
    state: com.studycircle.shared.features.circle.presentation.create.CreateCircleState,
    onNameChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("إنشاء حلقة جديدة", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            label = { Text("اسم الحلقة") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading) CircularProgressIndicator()
        else PrimaryButton(text = "إنشاء", onClick = onSubmit)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreateCirclePreview() {
    MaterialTheme {
        CreateCircleContent(
            state = com.studycircle.shared.features.circle.presentation.create.CreateCircleState(
                name = "حلقة جديدة"
            ),
            onNameChanged = {},
            onSubmit = {}
        )
    }
}
