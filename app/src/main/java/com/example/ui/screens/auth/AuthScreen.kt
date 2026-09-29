package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.CoinGold
import com.example.ui.theme.PrimaryIndigo

@Composable
fun AuthScreen(
    language: AppLanguage,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String, String, String) -> Unit,
    onQuickUserLogin: () -> Unit,
    onQuickAdminLogin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpTab by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("auth_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpTab) StringsLocalization.get("signup", language) else StringsLocalization.get("login", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab switch
            TabRow(
                selectedTabIndex = if (isSignUpTab) 1 else 0,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = !isSignUpTab,
                    onClick = { isSignUpTab = false },
                    text = { Text(StringsLocalization.get("login", language), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isSignUpTab,
                    onClick = { isSignUpTab = true },
                    text = { Text(StringsLocalization.get("signup", language), fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSignUpTab) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(StringsLocalization.get("full_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text(StringsLocalization.get("mobile", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(StringsLocalization.get("email", language)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(StringsLocalization.get("password", language)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSignUpTab) {
                        onSignup(email, password, name, mobile)
                    } else {
                        onLogin(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSignUpTab) StringsLocalization.get("create_account", language) else StringsLocalization.get("sign_in_button", language),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Instant 1-Click Testing",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onQuickUserLogin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Player Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onQuickAdminLogin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Admin Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
