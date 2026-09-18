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

import androidx.compose.ui.tooling.preview.Preview
import com.studycircle.shared.features.circle.presentation.create.CreateCircleState

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
        onIntent = { viewModel.dispatch(it) }
    )
}

@Composable
fun CreateCircleContent(
    state: CreateCircleState,
    onIntent: (CreateCircleIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("إنشاء حلقة جديدة", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(CreateCircleIntent.NameChanged(it)) },
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
        else PrimaryButton(text = "إنشاء", onClick = { onIntent(CreateCircleIntent.Submit) })
    }
}

@Preview(showBackground = true)
@Composable
fun CreateCircleContentPreview() {
    MaterialTheme {
        CreateCircleContent(
            state = CreateCircleState(),
            onIntent = {}
        )
    }
}
