package com.studycircle.android.ui.circle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.PrimaryButton

// Placeholder landing screen for Phase 1 — becomes the real circle list once
// tasks exist to show on it (Phase 2). Both actions show regardless of role
// for now; the backend already enforces SUPERVISOR/STUDENT via
// @PreAuthorize, so a wrong tap fails safely. Filtering by the logged-in
// user's role client-side is a Phase 2 polish item, not a security gap.
@Composable
fun CircleHomeScreen(onCreateCircle: () -> Unit, onJoinCircle: () -> Unit) {
    CircleHomeContent(onCreateCircle = onCreateCircle, onJoinCircle = onJoinCircle)
}

@Composable
fun CircleHomeContent(onCreateCircle: () -> Unit, onJoinCircle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PrimaryButton(text = "إنشاء حلقة", onClick = onCreateCircle)
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = "الانضمام إلى حلقة", onClick = onJoinCircle)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CircleHomePreview() {
    MaterialTheme {
        CircleHomeContent(onCreateCircle = {}, onJoinCircle = {})
    }
}
