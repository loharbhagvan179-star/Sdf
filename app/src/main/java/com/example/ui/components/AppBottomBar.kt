package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.CoinGold
import com.example.ui.theme.PrimaryIndigo

sealed class NavItem(
    val route: String,
    val stringKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavItem("home", "home", Icons.Filled.Home, Icons.Outlined.Home)
    object Quiz : NavItem("quiz", "quiz", Icons.Filled.Psychology, Icons.Outlined.Psychology)
    object Tasks : NavItem("tasks", "tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline)
    object Wallet : NavItem("wallet", "wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    object Profile : NavItem("profile", "profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
}

val NAV_ITEMS = listOf(
    NavItem.Home,
    NavItem.Quiz,
    NavItem.Tasks,
    NavItem.Wallet,
    NavItem.Profile
)

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("app_bottom_bar"),
        windowInsets = WindowInsets.navigationBars,
        tonalElevation = 8.dp
    ) {
        NAV_ITEMS.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = StringsLocalization.get(item.stringKey, language)
                    )
                },
                label = {
                    Text(text = StringsLocalization.get(item.stringKey, language))
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
