package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.StringsLocalization
import com.example.ui.theme.CoinGold
import com.example.ui.theme.PrimaryIndigo

enum class AuthMode {
    EMAIL,
    MOBILE
}

@Composable
fun AuthScreen(
    language: AppLanguage,
    onLoginWithEmail: (String, String) -> Unit,
    onLoginWithMobile: (String, String) -> Unit,
    onSignupWithEmail: (String, String, String, String) -> Unit,
    onSignupWithMobile: (String, String, String) -> Unit,
    onQuickUserLogin: () -> Unit,
    onQuickAdminLogin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpTab by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf(AuthMode.MOBILE) } // Default to Mobile sign in as requested

    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("auth_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSignUpTab) {
                            if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create Account"
                        } else {
                            if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "ईमेल या मोबाइल नंबर से साइन इन करें"
                        else
                            "Sign in with Email or Mobile Number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bonus Banner (200 Coins Download & Sign up reward!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CoinGold.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoinGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🪙", fontSize = 24.sp)
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "डाउनलोड एवं साइन-अप बोनस: +200 कॉइन"
                            else
                                "Download & Sign-up Bonus: +200 Coins",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "नया खाता बनाते ही ₹200 वैल्यू के कॉइन वॉलेट हिस्ट्री में मिलेंगे!"
                            else
                                "Get 200 welcome coins in wallet history instantly!",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Login / Signup Tab
            TabRow(
                selectedTabIndex = if (isSignUpTab) 1 else 0,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = !isSignUpTab,
                    onClick = { isSignUpTab = false },
                    text = { Text(if (language == AppLanguage.HINDI) "साइन इन" else "Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isSignUpTab,
                    onClick = { isSignUpTab = true },
                    text = { Text(if (language == AppLanguage.HINDI) "रजिस्टर" else "Register", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Method Selector: Mobile Number vs Email
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = { authMode = AuthMode.MOBILE },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (authMode == AuthMode.MOBILE) PrimaryIndigo else Color.Transparent,
                        contentColor = if (authMode == AuthMode.MOBILE) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "मोबाइल नंबर" else "Mobile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = { authMode = AuthMode.EMAIL },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (authMode == AuthMode.EMAIL) PrimaryIndigo else Color.Transparent,
                        contentColor = if (authMode == AuthMode.EMAIL) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "ईमेल" else "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Name field for registration
            if (isSignUpTab) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == AppLanguage.HINDI) "आपका नाम" else "Your Name") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Input fields depending on AuthMode
            if (authMode == AuthMode.MOBILE) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) mobile = it },
                    label = { Text(if (language == AppLanguage.HINDI) "10-अंकों का मोबाइल नंबर" else "10-Digit Mobile Number") },
                    leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null) },
                    prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (language == AppLanguage.HINDI) "ईमेल आईडी दर्ज करें" else "Enter Email Address") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (language == AppLanguage.HINDI) "पासवर्ड" else "Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (authMode == AuthMode.MOBILE) {
                        if (isSignUpTab) {
                            onSignupWithMobile(mobile, name, password)
                        } else {
                            onLoginWithMobile(mobile, password)
                        }
                    } else {
                        if (isSignUpTab) {
                            onSignupWithEmail(email, password, name, mobile)
                        } else {
                            onLoginWithEmail(email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text(
                    text = if (isSignUpTab) {
                        if (language == AppLanguage.HINDI) "खाता बनाएं (+200 कॉइन पाएं)" else "Create Account (+200 Coins)"
                    } else {
                        if (authMode == AuthMode.MOBILE) {
                            if (language == AppLanguage.HINDI) "मोबाइल से साइन इन करें" else "Sign In with Mobile"
                        } else {
                            if (language == AppLanguage.HINDI) "ईमेल से साइन इन करें" else "Sign In with Email"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Instant 1-Click Demo",
                style = MaterialTheme.typography.labelSmall,
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
