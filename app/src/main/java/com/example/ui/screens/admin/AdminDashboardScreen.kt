package com.example.ui.screens.admin

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    stats: AdminDashboardStats,
    users: List<User>,
    redeemRequests: List<RedeemRequest>,
    redeemCodes: List<RedeemCode>,
    quizzes: List<Quiz>,
    tasks: List<EarnTask>,
    language: AppLanguage,
    onApproveRequest: (String) -> Unit,
    onRejectRequest: (String, String) -> Unit,
    onAddRedeemCode: (String, String, Int) -> Unit,
    onToggleBlockUser: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Overview", "Redemptions", "Codes", "Users", "Quizzes & Tasks")

    var showAddCodeDialog by remember { mutableStateOf(false) }
    var newCodeInput by remember { mutableStateOf("") }
    var newCodeDenom by remember { mutableStateOf(20) }
    var newCodeType by remember { mutableStateOf(PayoutMethod.GOOGLE_PLAY.name) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Top Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                    Text(
                        text = StringsLocalization.get("admin_panel", language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Scrollable Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> AdminOverviewTab(stats = stats, language = language)
            1 -> AdminRedemptionsTab(
                requests = redeemRequests,
                onApprove = onApproveRequest,
                onReject = { id -> onRejectRequest(id, "Account or details verification failed") },
                language = language
            )
            2 -> AdminCodesTab(
                codes = redeemCodes,
                onOpenAddCode = { showAddCodeDialog = true },
                language = language
            )
            3 -> AdminUsersTab(
                users = users,
                onToggleBlock = onToggleBlockUser,
                language = language
            )
            4 -> AdminQuizzesAndTasksTab(
                quizzes = quizzes,
                tasks = tasks,
                language = language
            )
        }
    }

    // Add Code Dialog
    if (showAddCodeDialog) {
        AlertDialog(
            onDismissRequest = { showAddCodeDialog = false },
            title = { Text("Add Valid Redeem Code") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Codes are securely stored server-side and assigned only when admin approves a redeem request.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCodeInput,
                        onValueChange = { newCodeInput = it },
                        label = { Text("Code (e.g. PLAY-GIFT-98214)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20, 40, 100).forEach { amount ->
                            FilterChip(
                                selected = newCodeDenom == amount,
                                onClick = { newCodeDenom = amount },
                                label = { Text("₹$amount") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCodeInput.isNotBlank()) {
                            onAddRedeemCode(newCodeInput, newCodeType, newCodeDenom)
                            newCodeInput = ""
                            showAddCodeDialog = false
                        }
                    }
                ) {
                    Text("Add Code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(stats: AdminDashboardStats, language: AppLanguage) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Platform Key Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = StringsLocalization.get("total_users", language),
                    value = "${stats.totalUsers}",
                    icon = Icons.Default.People,
                    color = PrimaryIndigo,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = StringsLocalization.get("coins_in_circ", language),
                    value = "%,d".format(stats.totalCoinsInCirculation),
                    icon = Icons.Default.MonetizationOn,
                    color = CoinGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = StringsLocalization.get("pending_redemptions", language),
                    value = "${stats.pendingRedemptions}",
                    icon = Icons.Default.PendingActions,
                    color = WarningOrange,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = StringsLocalization.get("total_paid_out", language),
                    value = "₹${stats.totalPaidOutInr}",
                    icon = Icons.Default.CurrencyRupee,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Quizzes Completed",
                    value = "${stats.totalCompletedQuizzes}",
                    icon = Icons.Default.Quiz,
                    color = PrimaryIndigoLight,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Tasks Completed",
                    value = "${stats.totalCompletedTasks}",
                    icon = Icons.Default.CheckCircle,
                    color = EmeraldSuccessDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Health & Backend Status",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Dual-engine active (Cloud Firestore + In-App Secure Ledger).\n• Rate limiting: Max 1 daily bonus claim per 24 hours.\n• Duplicate reward filter: Enforced on all quizzes and tasks.\n• Redeem Code Vault: Server-side one-time assignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminRedemptionsTab(
    requests: List<RedeemRequest>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    language: AppLanguage
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No redemption requests found.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(requests) { req ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${req.userName} (₹${req.rewardAmountInInr})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${req.payoutMethod}: ${req.accountDetails}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (req.status) {
                                RedeemStatus.COMPLETED.name -> EmeraldSuccess.copy(alpha = 0.15f)
                                RedeemStatus.REJECTED.name -> CoralError.copy(alpha = 0.15f)
                                else -> WarningOrange.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = req.status,
                                color = when (req.status) {
                                    RedeemStatus.COMPLETED.name -> EmeraldSuccess
                                    RedeemStatus.REJECTED.name -> CoralError
                                    else -> WarningOrange
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Coins Reserved: ${req.coinsUsed} | Request ID: ${req.id}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (req.utrNumber != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚡ UPI Bank Reference: ${req.utrNumber}",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            fontSize = 13.sp
                        )
                    } else if (req.redeemCode != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Assigned Code: ${req.redeemCode}",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            fontSize = 13.sp
                        )
                    }

                    if (req.status == RedeemStatus.PENDING.name) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReject(req.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
                            ) {
                                Text("Reject & Refund", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onApprove(req.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                            ) {
                                Text(
                                    if (req.payoutMethod == PayoutMethod.UPI_CASHBACK.name) "Transfer UPI" else "Send Code",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCodesTab(
    codes: List<RedeemCode>,
    onOpenAddCode: () -> Unit,
    language: AppLanguage
) {
    val availableCount = codes.count { it.status == "AVAILABLE" }
    val assignedCount = codes.count { it.status == "ASSIGNED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Redeem Codes Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$availableCount Available / $assignedCount Assigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenAddCode,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Code")
                }
            }
        }

        items(codes) { codeItem ->
            val isAvailable = codeItem.status == "AVAILABLE"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = codeItem.code,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "₹${codeItem.denominationInInr} ${codeItem.rewardType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAvailable) EmeraldSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = codeItem.status,
                            color = if (isAvailable) EmeraldSuccess else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(
    users: List<User>,
    onToggleBlock: (String) -> Unit,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users) { u ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = u.displayName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (u.role == "admin") {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        color = PrimaryIndigo,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = u.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Coins: %,d | Quizzes: ${u.completedQuizIds.size} | Tasks: ${u.completedTaskIds.size}".format(u.coins),
                            fontSize = 11.sp,
                            color = CoinGold
                        )
                    }

                    if (u.role != "admin") {
                        FilledTonalButton(
                            onClick = { onToggleBlock(u.uid) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (u.isBlocked) EmeraldSuccess.copy(alpha = 0.2f) else CoralError.copy(alpha = 0.2f),
                                contentColor = if (u.isBlocked) EmeraldSuccess else CoralError
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (u.isBlocked) "Unblock" else "Block",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminQuizzesAndTasksTab(
    quizzes: List<Quiz>,
    tasks: List<EarnTask>,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Active Quizzes (${quizzes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(quizzes) { q ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = q.title, fontWeight = FontWeight.Bold)
                        Text(text = "+${q.coinReward} Coins", color = CoinGold, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Category: ${q.category} | ${q.questionCount} Questions | ${q.timeLimitSeconds}s limit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Active Tasks (${tasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(tasks) { t ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = t.title, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${t.category} | Reward: +${t.rewardCoins} Coins",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Active",
                            color = EmeraldSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
