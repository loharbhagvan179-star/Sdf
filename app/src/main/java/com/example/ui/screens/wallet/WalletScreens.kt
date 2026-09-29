package com.example.ui.screens.wallet

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.screens.home.TransactionRowItem
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    user: User?,
    transactions: List<Transaction>,
    language: AppLanguage,
    onNavigateToRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCoins = user?.coins ?: 0L
    var filterType by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(filterType, transactions) {
        when (filterType) {
            "EARNED" -> transactions.filter { it.amount > 0 }
            "REDEEMED" -> transactions.filter { it.amount < 0 }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("wallet_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Big Wallet Balance Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = StringsLocalization.get("wallet_balance", language),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CoinGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFF78350F),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "%,d".format(totalCoins),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoinGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "≈ ₹%.2f INR (Real Value)".format(totalCoins * (20f / 1200f)),
                            color = CoinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onNavigateToRedeem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (totalCoins >= 600) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redeem,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = StringsLocalization.get("redeem_now", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Ledger Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringsLocalization.get("transaction_history", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = filterType == "ALL",
                        onClick = { filterType = "ALL" },
                        label = { Text("All", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = filterType == "EARNED",
                        onClick = { filterType = "EARNED" },
                        label = { Text("Earned", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = filterType == "REDEEMED",
                        onClick = { filterType = "REDEEMED" },
                        label = { Text("Redeemed", fontSize = 12.sp) }
                    )
                }
            }
        }

        // Transaction History List
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = StringsLocalization.get("no_activity", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionRowItem(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemScreen(
    user: User?,
    redeemRequests: List<RedeemRequest>,
    language: AppLanguage,
    onSubmitRedeem: (RedeemTier, PayoutMethod, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalCoins = user?.coins ?: 0L

    // Category Tabs: 0: All, 1: Play Store, 2: UPI Cashback
    var selectedCategoryTab by remember { mutableStateOf(1) } // default to Play Store as popular!
    var selectedTierIndex by remember { mutableStateOf(1) } // ₹20 standard default
    var selectedPayoutMethod by remember { mutableStateOf(PayoutMethod.GOOGLE_PLAY) }
    var accountInput by remember { mutableStateOf("") }

    var showCelebrationDialog by remember { mutableStateOf(false) }
    var activeCelebrationRequest by remember { mutableStateOf<RedeemRequest?>(null) }

    LaunchedEffect(redeemRequests) {
        if (showCelebrationDialog && redeemRequests.isNotEmpty()) {
            activeCelebrationRequest = redeemRequests.first()
        }
    }

    // Sync method when tab changes
    LaunchedEffect(selectedCategoryTab) {
        when (selectedCategoryTab) {
            1 -> selectedPayoutMethod = PayoutMethod.GOOGLE_PLAY
            2 -> selectedPayoutMethod = PayoutMethod.UPI_CASHBACK
            else -> {}
        }
    }

    val currentTier = DEFAULT_REDEEM_TIERS.getOrElse(selectedTierIndex) { DEFAULT_REDEEM_TIERS[1] }
    val hasEnoughCoins = totalCoins >= currentTier.coinsRequired
    val neededCoins = (currentTier.coinsRequired - totalCoins).coerceAtLeast(0L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("redeem_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = StringsLocalization.get("choose_reward", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Play Store Redeem Code & UPI Cashback",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Category Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedCategoryTab == 1,
                    onClick = { selectedCategoryTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🎮")
                            Text(
                                text = if (language == AppLanguage.HINDI) "प्ले स्टोर कोड" else "Play Store",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedCategoryTab == 2,
                    onClick = { selectedCategoryTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡")
                            Text(
                                text = if (language == AppLanguage.HINDI) "UPI कैशबैक" else "UPI Cashback",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedCategoryTab == 0,
                    onClick = { selectedCategoryTab = 0 },
                    text = {
                        Text(
                            text = StringsLocalization.get("tab_all_rewards", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Spotlight Feature Card based on selected tab
        item {
            if (selectedCategoryTab == 1) {
                // Play Store Redeem Code Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F281E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00875A))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00875A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shop,
                                        contentDescription = "Play Store",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = StringsLocalization.get("play_store_redeem_code", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Official 16-character Google Play Gift Code",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA7F3D0)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00875A).copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00875A))
                            ) {
                                Text(
                                    text = "100% Genuine",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = StringsLocalization.get("play_store_instructions", language),
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else if (selectedCategoryTab == 2) {
                // UPI Instant Cashback Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryIndigo)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "UPI",
                                        tint = CoinGold,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = StringsLocalization.get("upi_instant_cashback", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Google Pay • PhonePe • Paytm • BHIM",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFC7D2FE)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                            ) {
                                Text(
                                    text = StringsLocalization.get("instant_transfer_badge", language),
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = StringsLocalization.get("upi_cashback_note", language),
                            fontSize = 11.sp,
                            color = Color(0xFFE0E7FF),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // Rule Card (All Rewards)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigoDark)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CoinGold.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = CoinGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = StringsLocalization.get("rule_1200_coins", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Coins are securely reserved during redemption & verified server-side.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // Tier Selection Cards
        item {
            Text(
                text = "Select Denomination",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEFAULT_REDEEM_TIERS.forEachIndexed { index, tier ->
                    val isSelected = selectedTierIndex == index
                    val canAfford = totalCoins >= tier.coinsRequired

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTierIndex = index },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (tier.isPopular) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CoinGold.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "POPULAR",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CoinGoldDark,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            Text(
                                text = "₹${tier.amountInInr}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${tier.coinsRequired} Coins",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (canAfford) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Insufficient coins warning notice
        if (!hasEnoughCoins) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = StringsLocalization.get("insufficient_balance", language),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "You need $neededCoins more coins to unlock ₹${currentTier.amountInInr}. Play quizzes or complete tasks!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Payout Method Selector (if All tab is selected or customize)
        if (selectedCategoryTab == 0) {
            item {
                Text(
                    text = StringsLocalization.get("payout_method", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PayoutMethod.values().forEach { method ->
                        val isChosen = selectedPayoutMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPayoutMethod = method },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChosen) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isChosen) 1.5.dp else 1.dp,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isChosen,
                                    onClick = { selectedPayoutMethod = method }
                                )
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) method.titleHi else method.title,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (language == AppLanguage.HINDI) method.inputHintHi else method.inputHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account Details Input (UPI ID or Email)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (selectedPayoutMethod == PayoutMethod.UPI_CASHBACK) "Enter UPI ID" else "Delivery Details",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                OutlinedTextField(
                    value = accountInput,
                    onValueChange = { accountInput = it },
                    label = {
                        Text(
                            if (language == AppLanguage.HINDI) selectedPayoutMethod.inputHintHi
                            else selectedPayoutMethod.inputHint
                        )
                    },
                    placeholder = {
                        Text(
                            if (selectedPayoutMethod == PayoutMethod.UPI_CASHBACK) "e.g. 9876543210@paytm or user@okaxis"
                            else "e.g. yourname@gmail.com"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Quick UPI suffix chips
                if (selectedPayoutMethod == PayoutMethod.UPI_CASHBACK) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suffixes = listOf("@paytm", "@okaxis", "@ybl", "@upi")
                        suffixes.forEach { suf ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val prefix = accountInput.substringBefore("@")
                                        if (prefix.isNotBlank()) {
                                            accountInput = prefix + suf
                                        } else {
                                            accountInput = "9876543210$suf"
                                        }
                                    }
                            ) {
                                Text(
                                    text = suf,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    showCelebrationDialog = true
                    onSubmitRedeem(currentTier, selectedPayoutMethod, accountInput)
                    accountInput = ""
                },
                enabled = hasEnoughCoins && accountInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPayoutMethod == PayoutMethod.UPI_CASHBACK) EmeraldSuccess else PrimaryIndigo
                )
            ) {
                Icon(
                    imageVector = if (selectedPayoutMethod == PayoutMethod.UPI_CASHBACK) Icons.Default.FlashOn else Icons.Default.Redeem,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${StringsLocalization.get("submit_redeem", language)} (₹${currentTier.amountInInr})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Redemption History Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = StringsLocalization.get("redeem_history", language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (redeemRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No redemptions requested yet. Select Play Store Code or UPI Cashback above!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(redeemRequests) { req ->
                RedeemRequestCard(req = req, language = language)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Instant Code Celebration Dialog (Shows direct code without waiting!)
    if (showCelebrationDialog && activeCelebrationRequest != null) {
        val req = activeCelebrationRequest!!
        val clipboardManager = LocalClipboardManager.current

        Dialog(
            onDismissRequest = {
                showCelebrationDialog = false
                activeCelebrationRequest = null
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00875A).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎉", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI) "रिडीम कोड तुरंत प्राप्त हुआ!" else "Instant Redeem Code!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "बधाई हो! आपका ₹${req.rewardAmountInInr} का प्ले स्टोर रिडीम कोड तैयार है।"
                        else
                            "Congratulations! Your ₹${req.rewardAmountInInr} Play Store voucher is ready.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (req.redeemCode != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F281E),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00875A))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "OFFICIAL GOOGLE PLAY CODE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFA7F3D0),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = req.redeemCode,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    letterSpacing = 1.5.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(req.redeemCode))
                                    Toast.makeText(context, if (language == AppLanguage.HINDI) "कोड कॉपी हो गया!" else "Code copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "कॉपी करें" else "Copy Code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code=${req.redeemCode}"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        clipboardManager.setText(AnnotatedString(req.redeemCode))
                                        Toast.makeText(context, "Code copied! Open Play Store to redeem.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00875A))
                            ) {
                                Icon(imageVector = Icons.Default.Shop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "Play Store खोलें" else "Open Store", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "• Play Store में जाकर Profile -> Payments & Subscriptions -> Redeem Code में यह कोड पेस्ट करके ₹${req.rewardAmountInInr} बैलेंस जोड़ें।"
                        else
                            "• Go to Google Play Store -> Profile -> Payments & subscriptions -> Redeem code to claim your ₹${req.rewardAmountInInr} balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = {
                            showCelebrationDialog = false
                            activeCelebrationRequest = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (language == AppLanguage.HINDI) "ठीक है (पूर्ण)" else "Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RedeemRequestCard(req: RedeemRequest, language: AppLanguage) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isPlayStore = req.payoutMethod == PayoutMethod.GOOGLE_PLAY.name
    val isUpi = req.payoutMethod == PayoutMethod.UPI_CASHBACK.name

    val statusColor = when (req.status) {
        RedeemStatus.COMPLETED.name -> EmeraldSuccess
        RedeemStatus.REJECTED.name -> CoralError
        RedeemStatus.PROCESSING.name -> PrimaryIndigo
        else -> WarningOrange
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayStore) Color(0xFF00875A).copy(alpha = 0.2f)
                                else if (isUpi) PrimaryIndigo.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlayStore) Icons.Default.Shop else if (isUpi) Icons.Default.FlashOn else Icons.Default.Receipt,
                            contentDescription = null,
                            tint = if (isPlayStore) Color(0xFF00875A) else if (isUpi) PrimaryIndigo else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isPlayStore) "Play Store Code: ₹${req.rewardAmountInInr}"
                            else if (isUpi) "UPI Cashback: ₹${req.rewardAmountInInr}"
                            else "₹${req.rewardAmountInInr} ${req.payoutMethod}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "To: ${req.accountDetails}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = req.status,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coins Used: ${req.coinsUsed} • Ref ID: ${req.id}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Play Store Code Result UI
            if (req.status == RedeemStatus.COMPLETED.name && isPlayStore && req.redeemCode != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F281E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00875A))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎮 " + StringsLocalization.get("redeem_code", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0)
                            )

                            // Copy Button
                            FilledTonalButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(req.redeemCode))
                                    Toast.makeText(context, StringsLocalization.get("code_copied", language), Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF00875A),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(StringsLocalization.get("copy_code", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Big stylized Code Display
                        Text(
                            text = req.redeemCode,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open in Play Store Direct Button
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/redeem?code=${req.redeemCode}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    clipboardManager.setText(AnnotatedString(req.redeemCode))
                                    Toast.makeText(context, "Code copied! Open Play Store to redeem.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00875A))
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(StringsLocalization.get("open_play_store", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // UPI Cashback Result Receipt UI
            if (req.status == RedeemStatus.COMPLETED.name && isUpi) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Text(
                                text = StringsLocalization.get("cashback_credited", language),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldSuccess,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${StringsLocalization.get("upi_utr_ref", language)}: ${req.utrNumber ?: "UTR492048102948"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Credited directly to UPI ID: ${req.accountDetails}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // If REJECTED, show refund notice
            if (req.status == RedeemStatus.REJECTED.name) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Refunded: ${req.rejectionReason ?: "Coins returned to wallet."}",
                    color = CoralError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
