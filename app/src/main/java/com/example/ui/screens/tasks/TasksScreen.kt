package com.example.ui.screens.tasks

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarnTask
import com.example.data.model.User
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    user: User?,
    tasks: List<EarnTask>,
    verifyingTaskId: String?,
    language: AppLanguage,
    onExecuteTask: (EarnTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedCount = tasks.count { user?.completedTaskIds?.contains(it.id) == true }
    val totalCount = tasks.size

    val handleTaskAction = { task: EarnTask ->
        if (task.actionUrl.isNotBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.actionUrl))
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
        onExecuteTask(task)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Task Milestone Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = StringsLocalization.get("daily_tasks", language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = StringsLocalization.get("complete_earn", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoinGold,
                            contentColor = Color(0xFF1E1B4B)
                        ) {
                            Text(
                                text = "$completedCount / $totalCount Done",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldSuccess,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // List of tasks
        items(tasks) { task ->
            val isCompleted = user?.completedTaskIds?.contains(task.id) == true
            val isVerifying = verifyingTaskId == task.id
            val isInstagram = task.iconType == "INSTAGRAM"
            val isYouTube = task.iconType == "YOUTUBE"

            val icon = when (task.iconType) {
                "INSTAGRAM" -> Icons.Default.CameraAlt
                "YOUTUBE" -> Icons.Default.Subscriptions
                "CHECKIN" -> Icons.Default.EventAvailable
                "SHARE" -> Icons.Default.Share
                "SURVEY" -> Icons.Default.Poll
                "VIDEO" -> Icons.Default.PlayCircle
                "FOLLOW" -> Icons.Default.Campaign
                else -> Icons.Default.TaskAlt
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isCompleted && !isVerifying) Modifier.clickable { handleTaskAction(task) }
                        else Modifier
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isInstagram && !isCompleted) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFD1D1D).copy(alpha = 0.4f))
                else if (isYouTube && !isCompleted) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.4f))
                else null,
                elevation = CardDefaults.cardElevation(if (isCompleted) 1.dp else 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (isCompleted) {
                                            Modifier.background(EmeraldSuccess.copy(alpha = 0.15f))
                                        } else if (isInstagram) {
                                            Modifier.background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                                )
                                            )
                                        } else if (isYouTube) {
                                            Modifier.background(Color(0xFFFF0000))
                                        } else {
                                            Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else icon,
                                    contentDescription = null,
                                    tint = if (isCompleted) EmeraldSuccess
                                    else if (isInstagram || isYouTube) Color.White
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isInstagram) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFD1D1D).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "IG",
                                                color = Color(0xFFFD1D1D),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else if (isYouTube) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFF0000).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "YT",
                                                color = Color(0xFFFF0000),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Reward Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCompleted) EmeraldSuccess.copy(alpha = 0.15f) else CoinGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isCompleted) "✓ Claimed" else "+${task.rewardCoins} Coins",
                                color = if (isCompleted) EmeraldSuccess else CoinGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verification status or action button
                    if (isVerifying) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = StringsLocalization.get("verifying_task", language),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (!isCompleted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = StringsLocalization.get("status_pending", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            FilledTonalButton(
                                onClick = { handleTaskAction(task) },
                                shape = RoundedCornerShape(10.dp),
                                colors = if (isInstagram) {
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFFD1D1D).copy(alpha = 0.15f),
                                        contentColor = Color(0xFFFD1D1D)
                                    )
                                } else if (isYouTube) {
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFFF0000).copy(alpha = 0.15f),
                                        contentColor = Color(0xFFFF0000)
                                    )
                                } else {
                                    ButtonDefaults.filledTonalButtonColors()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInstagram) Icons.Default.OpenInNew
                                    else if (isYouTube) Icons.Default.PlayArrow
                                    else Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isInstagram) {
                                        if (language == AppLanguage.HINDI) "फॉलो करें (@devrajlohar0981)" else "Follow @devrajlohar0981"
                                    } else if (isYouTube) {
                                        if (language == AppLanguage.HINDI) "सब्सक्राइब करें (total video DK)" else "Subscribe total video DK"
                                    } else {
                                        StringsLocalization.get("start_task", language)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = StringsLocalization.get("status_completed", language) + " (Reward credited to wallet)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
