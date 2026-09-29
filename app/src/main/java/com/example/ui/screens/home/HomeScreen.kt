package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EarnTask
import com.example.data.model.Quiz
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    user: User?,
    quizzes: List<Quiz>,
    tasks: List<EarnTask>,
    transactions: List<Transaction>,
    language: AppLanguage,
    onNavigateToQuiz: (Quiz?) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToRedeem: () -> Unit,
    onClaimDailyBonus: () -> Unit,
    onClaimScratchReward: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalCoins = user?.coins ?: 0L
    val targetRedeemCoins = 1200L
    val progress = (totalCoins.toFloat() / targetRedeemCoins).coerceIn(0f, 1f)
    val coinsRemaining = (targetRedeemCoins - totalCoins).coerceAtLeast(0L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Banner Card with visual illustration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Try loading generated hero banner drawable
                    Image(
                        painter = painterResource(id = R.drawable.ic_quiz_banner),
                        contentDescription = "Quiz & Earn Hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xE60B0F19),
                                        Color(0x991E1B4B),
                                        Color(0x33000000)
                                    )
                                )
                            )
                    )

                    // Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoinGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoinGold)
                        ) {
                            Text(
                                text = "🏆 " + StringsLocalization.get("app_title", language),
                                color = CoinGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = StringsLocalization.get("earn_coins_desc", language),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Play quizzes, complete easy tasks & redeem real cash!",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Total Coins & Quick Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = StringsLocalization.get("total_coins", language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "%,d".format(totalCoins),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CoinGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "≈ ₹%.2f".format(totalCoins * (20f / 1200f)),
                                        color = CoinGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToRedeem,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (totalCoins >= 1200) EmeraldSuccess else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringsLocalization.get("redeem_now", language),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar towards ₹20 reward (1200 Coins)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = StringsLocalization.get("rule_1200_coins", language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (totalCoins >= 1200) {
                                    StringsLocalization.get("ready_to_redeem", language)
                                } else {
                                    "$coinsRemaining " + StringsLocalization.get("coins_needed", language)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalCoins >= 1200) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (totalCoins >= 1200) EmeraldSuccess else PrimaryIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick 3 Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = StringsLocalization.get("quiz", language),
                            subtitle = "Trivia",
                            icon = Icons.Default.Psychology,
                            color = PrimaryIndigo,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToQuiz(null) }
                        )

                        ActionCard(
                            title = StringsLocalization.get("tasks", language),
                            subtitle = "+50 Coins",
                            icon = Icons.Default.CheckCircle,
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToTasks
                        )

                        ActionCard(
                            title = StringsLocalization.get("wallet", language),
                            subtitle = "History",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = CoinGoldDark,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToWallet
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Featured Redeem Shortcuts: Play Store & UPI Cashback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F281E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00875A))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00875A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Shop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "प्ले स्टोर कोड" else "Play Store Code",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(text = "₹10 • ₹20 • ₹50", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigateToRedeem() },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E1B4B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = CoinGold, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "UPI कैशबैक" else "UPI Cashback",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(text = "⚡ Instant Transfer", fontSize = 9.sp, color = Color(0xFFC7D2FE))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Bonus Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CoinGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = CoinGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = StringsLocalization.get("daily_bonus", language),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Get +25 free coins every 24 hours!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onClaimDailyBonus,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CoinGold,
                            contentColor = Color(0xFF1E1B4B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = StringsLocalization.get("claim_now", language),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Official Creator Social Follow Card (Instagram devrajlohar0981 & YouTube total video DK)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = StringsLocalization.get("creator_channels", language),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoinGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+100 Coins",
                                color = CoinGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Instagram Card (@devrajlohar0981)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/devrajlohar0981"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                    onNavigateToTasks()
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF26101B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFD1D1D).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Text(text = "+50", color = CoinGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "@devrajlohar0981",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Instagram Follow",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }

                        // YouTube Card (total video DK)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=total+video+DK"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                    onNavigateToTasks()
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2B1113),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFF0000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Subscriptions, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Text(text = "+50", color = CoinGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "total video DK",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "YouTube Subscribe",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFECACA)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cool Feature: Lucky Scratch Card
        item {
            LuckyScratchCard(
                language = language,
                onRewardClaimed = onClaimScratchReward
            )
        }

        // Cool Feature: 7-Day Streak Booster
        item {
            StreakBoosterCard(
                language = language,
                currentDay = 3
            )
        }

        // Cool Feature: Refer & Earn
        item {
            ReferAndEarnCard(
                language = language
            )
        }

        // Cool Feature: Leaderboard Preview
        item {
            LeaderboardPreviewCard(
                language = language
            )
        }

        // Featured Quizzes Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringsLocalization.get("featured_quizzes", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToQuiz(null) }) {
                    Text(
                        text = StringsLocalization.get("view_all", language),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Quizzes Horizontal Scroll
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quizzes) { quiz ->
                    val isCompleted = user?.completedQuizIds?.contains(quiz.id) == true
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { onNavigateToQuiz(quiz) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (quiz.isDaily) Color(0xFFEF4444).copy(alpha = 0.15f) else PrimaryIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (quiz.isDaily) "🔥 DAILY" else quiz.category,
                                        color = if (quiz.isDaily) Color(0xFFEF4444) else PrimaryIndigo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CoinGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+${quiz.coinReward} Coins",
                                        color = CoinGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = quiz.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = quiz.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${quiz.questionCount} Questions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isCompleted) {
                                    Text(
                                        text = "✓ Claimed",
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = "Play >",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Available Tasks Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringsLocalization.get("pending_tasks", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTasks) {
                    Text(
                        text = StringsLocalization.get("view_all", language),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top 2 Tasks preview
        items(tasks.take(2)) { task ->
            val isCompleted = user?.completedTaskIds?.contains(task.id) == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTasks() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isIg = task.iconType == "INSTAGRAM"
                    val isYt = task.iconType == "YOUTUBE"
                    val taskIcon = when (task.iconType) {
                        "INSTAGRAM" -> Icons.Default.CameraAlt
                        "YOUTUBE" -> Icons.Default.Subscriptions
                        else -> Icons.Default.TaskAlt
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isIg) Color(0xFFFD1D1D)
                                    else if (isYt) Color(0xFFFF0000)
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = taskIcon,
                                contentDescription = null,
                                tint = if (isIg || isYt) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCompleted) EmeraldSuccess.copy(alpha = 0.15f) else CoinGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isCompleted) "Completed" else "+50 Coins",
                            color = if (isCompleted) EmeraldSuccess else CoinGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Recent Activity / Transactions Section Header
        item {
            Text(
                text = StringsLocalization.get("recent_activity", language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = StringsLocalization.get("no_activity", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(transactions.take(4)) { tx ->
                TransactionRowItem(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionRowItem(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (tx.amount > 0) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.amount > 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = if (tx.amount > 0) EmeraldSuccess else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = tx.title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (tx.amount > 0) "+%,d".format(tx.amount) else "%,d".format(tx.amount),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (tx.amount > 0) EmeraldSuccess else Color(0xFFEF4444)
            )
        }
    }
}
