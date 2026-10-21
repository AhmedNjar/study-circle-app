package com.studycircle.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class NavTab(val id: String, val label: String) {
    HOME("home", "الرئيسية"),
    CIRCLES("circles", "الحلقات"),
    TASKS("tasks", "المهام"),
    PROFILE("profile", "الملف")
}

@Composable
fun AppMark(subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFD4AF37),
                            Color(0xFFC9A227)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = "المحفِظ والمتعلم",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DotsDivider(modifier: Modifier = Modifier) {
    val gold = Color(0xFFD4AF37)
    val teal = MaterialTheme.colorScheme.primary
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        listOf(teal, gold, teal).forEach { color ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IslamicBackground(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    Canvas(modifier = modifier) {
        val size = 60.dp.toPx()
        val columns = (this.size.width / size).toInt() + 1
        val rows = (this.size.height / size).toInt() + 1

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                val center = Offset(i * size, j * size)
                drawIslamicStar(center, size * 0.4f, color)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIslamicStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        for (i in 0 until 8) {
            val angle = i * 45f
            val r = if ((i % 2) == 0) radius else radius * 0.7f
            val rad = (angle * PI / 180f).toFloat()
            val x = center.x + r * cos(rad.toDouble()).toFloat()
            val y = center.y + r * sin(rad.toDouble()).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color, style = Stroke(width = 1.dp.toPx()))
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp
            )
        )
        trailing?.invoke()
    }
}

@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        content = content
    )
}

@Composable
fun IslamicTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StudyCircleBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val active = tab == currentTab
                val activeColor = Color(0xFFD4AF37)
                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) activeColor.copy(alpha = 0.15f) else Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NavTabIcon(
                            tab = tab,
                            color = if (active) activeColor else inactiveColor,
                            active = active
                        )
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) activeColor else inactiveColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavTabIcon(tab: NavTab, color: Color, active: Boolean) {
    Canvas(modifier = Modifier.size(22.dp)) {
        when (tab) {
            NavTab.HOME -> {
                val path = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.45f)
                    lineTo(size.width * 0.5f, size.height * 0.15f)
                    lineTo(size.width * 0.85f, size.height * 0.45f)
                    lineTo(size.width * 0.85f, size.height * 0.85f)
                    lineTo(size.width * 0.65f, size.height * 0.85f)
                    lineTo(size.width * 0.65f, size.height * 0.6f)
                    lineTo(size.width * 0.35f, size.height * 0.6f)
                    lineTo(size.width * 0.35f, size.height * 0.85f)
                    lineTo(size.width * 0.15f, size.height * 0.85f)
                    close()
                }
                if (active) drawPath(path, color = color)
                drawPath(path, color = color, style = Stroke(width = 1.8.dp.toPx()))
            }
            NavTab.CIRCLES -> {
                drawCircle(color, radius = size.width * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.8.dp.toPx()))
                val r = size.width * 0.09f
                drawCircle(color, radius = r, center = Offset(size.width * 0.32f, size.height * 0.42f))
                drawCircle(color, radius = r, center = Offset(size.width * 0.68f, size.height * 0.42f))
                drawCircle(color, radius = r, center = Offset(size.width * 0.5f, size.height * 0.68f))
            }
            NavTab.TASKS -> {
                val rectPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(size.width * 0.15f, size.height * 0.15f, size.width * 0.85f, size.height * 0.85f),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    )
                }
                if (active) drawPath(rectPath, color = color.copy(alpha = 0.2f))
                drawPath(rectPath, color = color, style = Stroke(width = 1.8.dp.toPx()))
                val checkPath = Path().apply {
                    moveTo(size.width * 0.32f, size.height * 0.5f)
                    lineTo(size.width * 0.45f, size.height * 0.65f)
                    lineTo(size.width * 0.7f, size.height * 0.38f)
                }
                drawPath(checkPath, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            NavTab.PROFILE -> {
                drawCircle(color, radius = size.width * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.35f), style = Stroke(width = 1.8.dp.toPx()))
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.85f)
                    cubicTo(
                        size.width * 0.18f, size.height * 0.65f,
                        size.width * 0.82f, size.height * 0.65f,
                        size.width * 0.82f, size.height * 0.85f
                    )
                }
                drawPath(path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}
