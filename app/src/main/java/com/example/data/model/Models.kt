package com.example.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val mobile: String = "",
    val coins: Long = 0,
    val completedQuizIds: List<String> = emptyList(),
    val completedTaskIds: List<String> = emptyList(),
    val lastDailyBonusClaimTime: Long = 0L,
    val role: String = "user", // "user" or "admin"
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Quiz(
    val id: String = "",
    val title: String = "",
    val category: String = "", // "GK", "Science", "Sports", "Technology"
    val description: String = "",
    val coinReward: Int = 30,
    val questionCount: Int = 5,
    val timeLimitSeconds: Int = 15,
    val isDaily: Boolean = false
)

data class Question(
    val id: String = "",
    val quizId: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = ""
)

data class EarnTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val rewardCoins: Int = 50,
    val category: String = "General",
    val actionUrl: String = "",
    val isEligible: Boolean = true,
    val iconType: String = "CHECKIN" // "CHECKIN", "SHARE", "SURVEY", "VIDEO", "FOLLOW", "QUIZ_STREAK"
)

enum class TransactionType {
    EARN_QUIZ,
    EARN_TASK,
    EARN_DAILY,
    REDEEM_DEDUCT,
    REDEEM_REFUND
}

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Int = 0, // positive for earn, negative for redeem deduct
    val type: String = "EARN_QUIZ",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)

enum class RedeemStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    REJECTED
}

enum class PayoutMethod(
    val title: String,
    val titleHi: String,
    val inputHint: String,
    val inputHintHi: String,
    val category: String
) {
    GOOGLE_PLAY(
        "Play Store Redeem Code",
        "प्ले स्टोर रिडीम कोड",
        "Enter Email / Phone for delivery",
        "ईमेल या फ़ोन नंबर दर्ज करें",
        "PLAY_STORE"
    ),
    UPI_CASHBACK(
        "UPI Instant Cashback",
        "यूपीआई कैशबैक",
        "Enter UPI ID (e.g. mobile@upi, user@paytm, user@okaxis)",
        "अपनी UPI ID दर्ज करें (उदा: 9876543210@paytm, user@ybl)",
        "UPI"
    ),
    PAYTM_UPI(
        "Paytm Wallet Cash",
        "पेटीएम वॉलेट कैश",
        "Enter 10-digit Paytm registered mobile number",
        "10 अंकों का पेटीएम मोबाइल नंबर दर्ज करें",
        "WALLET"
    ),
    AMAZON(
        "Amazon Pay Gift Voucher",
        "अमेज़न पे गिफ्ट वाउचर",
        "Enter Amazon Account Email / Phone",
        "अमेज़न अकाउंट ईमेल/फ़ोन दर्ज करें",
        "GIFT_CARD"
    )
}

data class RedeemTier(
    val coinsRequired: Int,
    val amountInInr: Int,
    val label: String,
    val isPopular: Boolean = false
)

val DEFAULT_REDEEM_TIERS = listOf(
    RedeemTier(600, 10, "₹10 Instant Reward"),
    RedeemTier(1200, 20, "₹20 Cash / Code", isPopular = true),
    RedeemTier(3000, 50, "₹50 Big Reward"),
    RedeemTier(6000, 100, "₹100 Mega Reward")
)

data class RedeemRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val coinsUsed: Int = 1200,
    val rewardAmountInInr: Int = 20,
    val payoutMethod: String = PayoutMethod.GOOGLE_PLAY.name,
    val accountDetails: String = "",
    val status: String = RedeemStatus.PENDING.name,
    val redeemCode: String? = null,
    val utrNumber: String? = null, // for UPI Cashback
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val rejectionReason: String? = null
)

data class RedeemCode(
    val id: String = "",
    val code: String = "",
    val rewardType: String = PayoutMethod.GOOGLE_PLAY.name,
    val denominationInInr: Int = 20,
    val status: String = "AVAILABLE", // "AVAILABLE" or "ASSIGNED"
    val assignedToUserId: String? = null,
    val assignedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val totalCoinsInCirculation: Long = 0,
    val pendingRedemptions: Int = 0,
    val totalPaidOutInr: Int = 0,
    val totalCompletedQuizzes: Int = 0,
    val totalCompletedTasks: Int = 0
)
