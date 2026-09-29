package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.CoinGold
import com.example.ui.theme.PrimaryIndigo

@Composable
fun SubscribeChannelsDialog(
    language: AppLanguage,
    onClaimReward: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var youtubeVisited by remember { mutableStateOf(false) }
    var instagramVisited by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoinGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🎁 +100 Coins",
                            color = CoinGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (language == AppLanguage.HINDI) "हमारे दोनों चैनल सब्सक्राइब करें!" else "Subscribe to Our 2 Channels!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (language == AppLanguage.HINDI)
                        "साइन इन पूरा करने और +100 कॉइन रिवॉर्ड पाने के लिए नीचे दिए गए दोनों चैनल को सब्सक्राइब व फॉलो करें।"
                    else
                        "Subscribe & follow both official channels to unlock exclusive daily redeem codes & earn +100 coins!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Channel 1: YouTube
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            youtubeVisited = true
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=total+video+DK"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2E1012),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.5f))
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF0000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "total video DK",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (youtubeVisited) "✅ " + (if (language == AppLanguage.HINDI) "सब्सक्राइब विज़िटेड" else "Visited") else (if (language == AppLanguage.HINDI) "यूट्यूब पर सब्सक्राइब करें" else "Subscribe on YouTube"),
                                    fontSize = 11.sp,
                                    color = if (youtubeVisited) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                youtubeVisited = true
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=total+video+DK"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (youtubeVisited) (if (language == AppLanguage.HINDI) "हो गया" else "Done") else (if (language == AppLanguage.HINDI) "सब्सक्राइब" else "Subscribe"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Channel 2: Instagram
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            instagramVisited = true
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/devrajlohar0981"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF28101C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFD1D1D).copy(alpha = 0.5f))
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "@devrajlohar0981",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (instagramVisited) "✅ " + (if (language == AppLanguage.HINDI) "फॉलो विज़िटेड" else "Visited") else (if (language == AppLanguage.HINDI) "इंस्टाग्राम पर फॉलो करें" else "Follow on Instagram"),
                                    fontSize = 11.sp,
                                    color = if (instagramVisited) Color(0xFF86EFAC) else Color(0xFFFBCFE8)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                instagramVisited = true
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/devrajlohar0981"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD1D1D)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (instagramVisited) (if (language == AppLanguage.HINDI) "हो गया" else "Done") else (if (language == AppLanguage.HINDI) "फॉलो" else "Follow"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Claim Bonus Button
                Button(
                    onClick = onClaimReward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "मैंने सब्सक्राइब कर लिया (+100 कॉइन पाएं)" else "I Have Subscribed (+100 Coins)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "बाद में करें" else "Maybe Later",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
