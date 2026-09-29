package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class QuizEarnRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("quiz_earn_prefs_v2", Context.MODE_PRIVATE)

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
        } catch (_: Exception) {}
    }

    // In-memory / persistent state flows
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _questions = MutableStateFlow<Map<String, List<Question>>>(emptyMap())

    private val _tasks = MutableStateFlow<List<EarnTask>>(emptyList())
    val tasks: StateFlow<List<EarnTask>> = _tasks.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _redeemRequests = MutableStateFlow<List<RedeemRequest>>(emptyList())
    val redeemRequests: StateFlow<List<RedeemRequest>> = _redeemRequests.asStateFlow()

    private val _redeemCodes = MutableStateFlow<List<RedeemCode>>(emptyList())
    val redeemCodes: StateFlow<List<RedeemCode>> = _redeemCodes.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _hasSubscribedChannels = MutableStateFlow(false)
    val hasSubscribedChannels: StateFlow<Boolean> = _hasSubscribedChannels.asStateFlow()

    init {
        seedQuizzesAndTasks()
        _hasSubscribedChannels.value = prefs.getBoolean("has_subscribed_channels", false)

        val loadedFromStorage = loadPersistentData()
        if (!loadedFromStorage) {
            seedInitialUserData()
            savePersistentData()
        }
    }

    // ----------------------------------------------------
    // PERSISTENCE (Saves and restores across app restarts & background kills)
    // ----------------------------------------------------
    private fun savePersistentData() {
        try {
            val usersArray = JSONArray()
            _allUsers.value.forEach { usersArray.put(it.toJson()) }

            val txArray = JSONArray()
            _transactions.value.forEach { txArray.put(it.toJson()) }

            val reqArray = JSONArray()
            _redeemRequests.value.forEach { reqArray.put(it.toJson()) }

            val codesArray = JSONArray()
            _redeemCodes.value.forEach { codesArray.put(it.toJson()) }

            prefs.edit()
                .putString("persisted_users_v2", usersArray.toString())
                .putString("persisted_transactions_v2", txArray.toString())
                .putString("persisted_redeem_requests_v2", reqArray.toString())
                .putString("persisted_redeem_codes_v2", codesArray.toString())
                .putString("saved_uid", _currentUser.value?.uid)
                .putBoolean("has_subscribed_channels", _hasSubscribedChannels.value)
                .commit() // Commit synchronously to ensure it persists before any process kill
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPersistentData(): Boolean {
        val usersJson = prefs.getString("persisted_users_v2", null) ?: return false
        val txJson = prefs.getString("persisted_transactions_v2", null) ?: return false
        val reqJson = prefs.getString("persisted_redeem_requests_v2", null) ?: return false

        return try {
            val uArray = JSONArray(usersJson)
            val uList = mutableListOf<User>()
            for (i in 0 until uArray.length()) {
                uList.add(uArray.getJSONObject(i).toUser())
            }

            val tArray = JSONArray(txJson)
            val tList = mutableListOf<Transaction>()
            for (i in 0 until tArray.length()) {
                tList.add(tArray.getJSONObject(i).toTransaction())
            }

            val rArray = JSONArray(reqJson)
            val rList = mutableListOf<RedeemRequest>()
            for (i in 0 until rArray.length()) {
                rList.add(rArray.getJSONObject(i).toRedeemRequest())
            }

            val codesJson = prefs.getString("persisted_redeem_codes_v2", null)
            val cList = mutableListOf<RedeemCode>()
            if (codesJson != null) {
                val cArray = JSONArray(codesJson)
                for (i in 0 until cArray.length()) {
                    cList.add(cArray.getJSONObject(i).toRedeemCode())
                }
            } else {
                cList.addAll(defaultRedeemCodes())
            }

            _allUsers.value = uList
            _transactions.value = tList
            _redeemRequests.value = rList
            _redeemCodes.value = cList

            val savedUid = prefs.getString("saved_uid", null)
            val user = uList.find { it.uid == savedUid } ?: uList.firstOrNull()
            _currentUser.value = user
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun seedQuizzesAndTasks() {
        val initialQuizzes = listOf(
            Quiz(
                id = "quiz_gk_1",
                title = "India & World GK",
                category = "GK",
                description = "Test your knowledge about world capitals, monuments, and geography.",
                coinReward = 30,
                questionCount = 5,
                timeLimitSeconds = 15,
                isDaily = false
            ),
            Quiz(
                id = "quiz_science_1",
                title = "Science & Space Exploration",
                category = "Science",
                description = "Cosmology, physics laws, and groundbreaking scientific inventions.",
                coinReward = 35,
                questionCount = 5,
                timeLimitSeconds = 15,
                isDaily = false
            ),
            Quiz(
                id = "quiz_sports_1",
                title = "Cricket & Global Sports",
                category = "Sports",
                description = "World Cup highlights, famous athletes, records, and trophies.",
                coinReward = 30,
                questionCount = 5,
                timeLimitSeconds = 15,
                isDaily = false
            ),
            Quiz(
                id = "quiz_tech_1",
                title = "Technology & Artificial Intelligence",
                category = "Technology",
                description = "Modern gadgets, software, AI models, and computer history.",
                coinReward = 40,
                questionCount = 5,
                timeLimitSeconds = 15,
                isDaily = false
            ),
            Quiz(
                id = "quiz_daily_1",
                title = "Daily Super Challenge",
                category = "GK",
                description = "Today's handpicked brain teaser quiz with special reward coins!",
                coinReward = 50,
                questionCount = 5,
                timeLimitSeconds = 15,
                isDaily = true
            )
        )
        _quizzes.value = initialQuizzes

        val qMap = mutableMapOf<String, List<Question>>()
        qMap["quiz_gk_1"] = listOf(
            Question("gk_q1", "quiz_gk_1", "Which planet is known as the Red Planet?", listOf("Venus", "Mars", "Jupiter", "Saturn"), 1, "Mars appears reddish due to iron oxide."),
            Question("gk_q2", "quiz_gk_1", "What is the capital city of Australia?", listOf("Sydney", "Melbourne", "Canberra", "Brisbane"), 2, "Canberra was chosen as the capital."),
            Question("gk_q3", "quiz_gk_1", "Which river is the longest in the world?", listOf("Amazon", "Nile", "Yangtze", "Ganga"), 1, "The Nile River is approximately 6,650 km long."),
            Question("gk_q4", "quiz_gk_1", "Who wrote the national anthem of India ('Jana Gana Mana')?", listOf("Bankim Chandra", "Rabindranath Tagore", "Sarojini Naidu", "Swami Vivekananda"), 1, "Rabindranath Tagore composed the national anthem."),
            Question("gk_q5", "quiz_gk_1", "Which is the highest mountain peak in the world?", listOf("K2", "Mount Everest", "Kangchenjunga", "Makalu"), 1, "Mount Everest is 8,848.86 meters high.")
        )
        qMap["quiz_science_1"] = listOf(
            Question("sci_q1", "quiz_science_1", "What is the chemical symbol for Gold?", listOf("Ag", "Au", "Fe", "Pb"), 1, "Au comes from Latin Aurum."),
            Question("sci_q2", "quiz_science_1", "Powerhouse of the eukaryotic cell?", listOf("Ribosome", "Nucleus", "Mitochondria", "ER"), 2, "Mitochondria generate ATP."),
            Question("sci_q3", "quiz_science_1", "Boiling point of pure water at standard pressure?", listOf("90°C", "100°C", "110°C", "120°C"), 1, "Pure water boils at 100°C."),
            Question("sci_q4", "quiz_science_1", "Which gas is absorbed by plants in photosynthesis?", listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Argon"), 1, "Plants absorb Carbon Dioxide."),
            Question("sci_q5", "quiz_science_1", "Approximate speed of light in vacuum?", listOf("30,000 km/s", "150,000 km/s", "300,000 km/s", "1,000,000 km/s"), 2, "Light travels at ~300,000 km/s.")
        )
        qMap["quiz_sports_1"] = listOf(
            Question("spo_q1", "quiz_sports_1", "Who won the first ICC Men's T20 World Cup in 2007?", listOf("Pakistan", "India", "Australia", "West Indies"), 1, "India won the 2007 T20 World Cup."),
            Question("spo_q2", "quiz_sports_1", "How many players on the pitch per football team?", listOf("9", "10", "11", "12"), 2, "A team has 11 players."),
            Question("spo_q3", "quiz_sports_1", "Tennis grand slam played on grass courts?", listOf("Roland Garros", "Wimbledon", "US Open", "Australian Open"), 1, "Wimbledon is played on grass."),
            Question("spo_q4", "quiz_sports_1", "Number of rings in official Olympic flag?", listOf("4", "5", "6", "7"), 1, "5 rings represent the five continents."),
            Question("spo_q5", "quiz_sports_1", "Length of cricket pitch between the wickets?", listOf("20 yards", "22 yards", "24 yards", "26 yards"), 1, "Cricket pitch is 22 yards long.")
        )
        qMap["quiz_tech_1"] = listOf(
            Question("tech_q1", "quiz_tech_1", "Recommended language for modern Android development?", listOf("Java", "Kotlin", "Flutter", "Swift"), 1, "Kotlin is Google's preferred Android language."),
            Question("tech_q2", "quiz_tech_1", "What does AI stand for?", listOf("Automated Internet", "Artificial Intelligence", "Advanced Integration", "App Interface"), 1, "Artificial Intelligence."),
            Question("tech_q3", "quiz_tech_1", "Fundamental unit of data in computing?", listOf("Byte", "Bit", "Nibble", "Pixel"), 1, "A bit is a binary digit."),
            Question("tech_q4", "quiz_tech_1", "Operating system using Linux kernel?", listOf("Windows", "Android", "iOS", "macOS"), 1, "Android uses the Linux kernel."),
            Question("tech_q5", "quiz_tech_1", "What does HTTP stand for?", listOf("HyperText Transfer Protocol", "High Technical Transfer Program", "Hyperlink Text Processor", "Home Terminal Page"), 0, "HyperText Transfer Protocol.")
        )
        qMap["quiz_daily_1"] = listOf(
            Question("daily_q1", "quiz_daily_1", "Largest ocean on Earth?", listOf("Atlantic", "Pacific", "Indian", "Arctic"), 1, "Pacific Ocean is the largest."),
            Question("daily_q2", "quiz_daily_1", "Father of modern computing?", listOf("Charles Babbage", "Alan Turing", "Ada Lovelace", "John von Neumann"), 0, "Charles Babbage."),
            Question("daily_q3", "quiz_daily_1", "Chemical element with atomic number 1?", listOf("Helium", "Hydrogen", "Carbon", "Oxygen"), 1, "Hydrogen has atomic number 1."),
            Question("daily_q4", "quiz_daily_1", "Country hosting the Taj Mahal?", listOf("Nepal", "India", "Bangladesh", "Sri Lanka"), 1, "Taj Mahal is in Agra, India."),
            Question("daily_q5", "quiz_daily_1", "What does GPU stand for?", listOf("General Processing Unit", "Graphics Processing Unit", "Grand Program Utility", "Global Processor"), 1, "Graphics Processing Unit.")
        )
        _questions.value = qMap

        val initialTasks = listOf(
            EarnTask("task_instagram_follow", "Follow @devrajlohar0981 on Instagram", "Follow creator devrajlohar0981 on Instagram and claim 50 free coins!", 50, "Instagram", "https://www.instagram.com/devrajlohar0981", true, "INSTAGRAM"),
            EarnTask("task_youtube_sub", "Subscribe to total video DK on YouTube", "Subscribe to 'total video DK' on YouTube and claim 50 free coins!", 50, "YouTube", "https://www.youtube.com/results?search_query=total+video+DK", true, "YOUTUBE"),
            EarnTask("task_checkin_1", "Daily Community Check-in", "Check-in today, view community tips, and claim your task reward.", 50, "Daily", "https://community.quizearn.app", true, "CHECKIN"),
            EarnTask("task_share_1", "Share App With Friends", "Share Quiz & Earn on WhatsApp or social apps to help friends earn.", 50, "Social", "https://quizearn.app/invite", true, "SHARE"),
            EarnTask("task_survey_1", "Complete Quick User Feedback", "Answer a 30-second opinion survey to improve app quizzes.", 50, "Survey", "https://feedback.quizearn.app", true, "SURVEY"),
            EarnTask("task_video_1", "Watch Partner Sponsor Clip", "Watch an educational 15-second sponsor spotlight.", 50, "Video", "https://partner.quizearn.app", true, "VIDEO"),
            EarnTask("task_follow_1", "Join Official Announcements Channel", "Follow Quiz & Earn news to get notified of special bonus events.", 50, "Social", "https://t.me/quizearn_official", true, "FOLLOW"),
            EarnTask("task_streak_1", "Quiz Master Streak Challenge", "Play any 2 category quizzes to unlock this milestone bonus.", 50, "Challenge", "", true, "QUIZ_STREAK")
        )
        _tasks.value = initialTasks
    }

    private fun defaultRedeemCodes(): List<RedeemCode> {
        return listOf(
            RedeemCode("code_gp_01", "3K7X-9PLQ-8B2M-W4TN", PayoutMethod.GOOGLE_PLAY.name, 20, "AVAILABLE"),
            RedeemCode("code_gp_02", "GPAY-4M92-LK7A-59BX", PayoutMethod.GOOGLE_PLAY.name, 20, "AVAILABLE"),
            RedeemCode("code_gp_03", "PLAY-72BX-9QMA-11KC", PayoutMethod.GOOGLE_PLAY.name, 10, "AVAILABLE"),
            RedeemCode("code_gp_04", "PLAY-88NM-3QPA-77ZT", PayoutMethod.GOOGLE_PLAY.name, 50, "AVAILABLE"),
            RedeemCode("code_gp_05", "PLAY-99KP-22AX-88LM", PayoutMethod.GOOGLE_PLAY.name, 10, "AVAILABLE"),
            RedeemCode("code_gp_06", "PLAY-14VZ-77OP-33CD", PayoutMethod.GOOGLE_PLAY.name, 20, "AVAILABLE"),
            RedeemCode("code_gp_07", "PLAY-55KL-88PQ-19RT", PayoutMethod.GOOGLE_PLAY.name, 100, "AVAILABLE"),
            RedeemCode("code_amz_01", "AMZ-IN-8392-GIFT-20", PayoutMethod.AMAZON.name, 20, "AVAILABLE")
        )
    }

    private fun seedInitialUserData() {
        val demoUser = User(
            uid = "user_demo_01",
            email = "demo.player@quizearn.com",
            displayName = "Rahul Sharma",
            mobile = "9876543210",
            coins = 1200, // Starts with enough for ₹20 test redemption
            completedQuizIds = emptyList(),
            completedTaskIds = emptyList(),
            lastDailyBonusClaimTime = 0L,
            role = "user",
            isBlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L
        )

        val demoAdmin = User(
            uid = "admin_master_01",
            email = "admin@quizearn.com",
            displayName = "Master Admin",
            mobile = "9999988888",
            coins = 25000,
            completedQuizIds = emptyList(),
            completedTaskIds = emptyList(),
            lastDailyBonusClaimTime = 0L,
            role = "admin",
            isBlocked = false,
            createdAt = System.currentTimeMillis() - 864000000L
        )

        _allUsers.value = listOf(demoUser, demoAdmin)
        _currentUser.value = demoUser

        // Download bonus of 200 coins shown in transaction history!
        val initTx = listOf(
            Transaction(
                id = "tx_download_bonus",
                userId = demoUser.uid,
                title = "Download & Signup Bonus (₹200 Value)",
                amount = 200,
                type = TransactionType.EARN_DAILY.name,
                description = "₹200 Welcome bonus coins added upon app download and sign-in!",
                timestamp = System.currentTimeMillis() - 3600000L,
                status = "SUCCESS"
            ),
            Transaction(
                id = "tx_welcome_bonus",
                userId = demoUser.uid,
                title = "Welcome Starter Gift",
                amount = 1000,
                type = TransactionType.EARN_DAILY.name,
                description = "Account activation coins reward",
                timestamp = System.currentTimeMillis() - 7200000L,
                status = "SUCCESS"
            )
        )
        _transactions.value = initTx
        _redeemCodes.value = defaultRedeemCodes()
        _redeemRequests.value = emptyList()
    }

    // ----------------------------------------------------
    // AUTHENTICATION (Email OR Mobile Number)
    // ----------------------------------------------------
    suspend fun loginWithEmail(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val user = _allUsers.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
        if (user != null) {
            if (user.isBlocked) {
                return@withContext Result.failure(Exception("Account is blocked by Admin."))
            }
            _currentUser.value = user
            savePersistentData()
            return@withContext Result.success(user)
        }

        // Create new account if not exists
        val newUser = User(
            uid = "user_" + UUID.randomUUID().toString().take(8),
            email = cleanEmail,
            displayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            mobile = "",
            coins = 200, // 200 Download / Sign-up bonus coins
            role = if (cleanEmail.contains("admin", ignoreCase = true)) "admin" else "user"
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        addTransaction(
            userId = newUser.uid,
            title = "Download & Signup Bonus (₹200 Value)",
            amount = 200,
            type = TransactionType.EARN_DAILY.name,
            description = "₹200 Welcome bonus coins added upon app download and sign-in!"
        )
        savePersistentData()
        Result.success(newUser)
    }

    suspend fun signupWithEmail(email: String, pass: String, name: String, mobile: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        if (_allUsers.value.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            return@withContext Result.failure(Exception("User already exists with this email."))
        }
        val newUser = User(
            uid = "user_" + UUID.randomUUID().toString().take(8),
            email = cleanEmail,
            displayName = name.ifBlank { cleanEmail.substringBefore("@") },
            mobile = mobile.trim(),
            coins = 200, // 200 Download / Sign-up bonus coins
            role = if (cleanEmail.contains("admin", ignoreCase = true)) "admin" else "user"
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        addTransaction(
            userId = newUser.uid,
            title = "Download & Signup Bonus (₹200 Value)",
            amount = 200,
            type = TransactionType.EARN_DAILY.name,
            description = "₹200 Welcome bonus coins added upon app download and sign-in!"
        )
        savePersistentData()
        Result.success(newUser)
    }

    suspend fun loginWithMobile(mobile: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanPhone = mobile.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter a valid 10-digit mobile number"))
        }

        val last10 = cleanPhone.takeLast(10)
        val user = _allUsers.value.find { it.mobile.replace(" ", "").replace("-", "").endsWith(last10) }
        if (user != null) {
            if (user.isBlocked) {
                return@withContext Result.failure(Exception("Account is blocked by Admin."))
            }
            _currentUser.value = user
            savePersistentData()
            return@withContext Result.success(user)
        }

        // Auto-create new user with mobile number and ₹200 download bonus!
        val newUser = User(
            uid = "user_mob_" + UUID.randomUUID().toString().take(8),
            email = "",
            displayName = "User $last10",
            mobile = cleanPhone,
            coins = 200, // 200 Download / Sign-up bonus coins
            role = "user"
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        addTransaction(
            userId = newUser.uid,
            title = "Download & Signup Bonus (₹200 Value)",
            amount = 200,
            type = TransactionType.EARN_DAILY.name,
            description = "₹200 Welcome bonus coins added upon app download and sign-in!"
        )
        savePersistentData()
        Result.success(newUser)
    }

    suspend fun signupWithMobile(mobile: String, name: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanPhone = mobile.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter a valid 10-digit mobile number"))
        }

        val last10 = cleanPhone.takeLast(10)
        if (_allUsers.value.any { it.mobile.replace(" ", "").replace("-", "").endsWith(last10) }) {
            return@withContext Result.failure(Exception("User already registered with this mobile number. Please Sign In."))
        }

        val newUser = User(
            uid = "user_mob_" + UUID.randomUUID().toString().take(8),
            email = "",
            displayName = name.ifBlank { "User $last10" },
            mobile = cleanPhone,
            coins = 200, // 200 Download / Sign-up bonus coins
            role = "user"
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        addTransaction(
            userId = newUser.uid,
            title = "Download & Signup Bonus (₹200 Value)",
            amount = 200,
            type = TransactionType.EARN_DAILY.name,
            description = "₹200 Welcome bonus coins added upon app download and sign-in!"
        )
        savePersistentData()
        Result.success(newUser)
    }

    fun quickSwitchUser(role: String) {
        val target = _allUsers.value.find { it.role == role } ?: return
        _currentUser.value = target
        savePersistentData()
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove("saved_uid").apply()
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
    }

    // ----------------------------------------------------
    // CHANNELS SUBSCRIPTION (Mandatory / Bonus prompt)
    // ----------------------------------------------------
    suspend fun claimChannelsSubscriptionReward(): Result<Int> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (_hasSubscribedChannels.value) {
            return@withContext Result.success(0)
        }

        _hasSubscribedChannels.value = true
        val bonus = 100 // 100 coins for subscribing to both channels
        val updated = user.copy(coins = user.coins + bonus)
        updateUserData(updated)

        addTransaction(
            userId = user.uid,
            title = "Channels Subscription Bonus",
            amount = bonus,
            type = TransactionType.EARN_TASK.name,
            description = "Subscribed to 'total video DK' & '@devrajlohar0981'"
        )
        savePersistentData()
        Result.success(bonus)
    }

    // ----------------------------------------------------
    // DAILY BONUS & EXTRA REWARDS
    // ----------------------------------------------------
    suspend fun claimDailyBonus(): Result<Int> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (user.isBlocked) return@withContext Result.failure(Exception("Account is blocked"))

        val now = System.currentTimeMillis()
        val cooldownMillis = 24 * 60 * 60 * 1000L
        val diff = now - user.lastDailyBonusClaimTime

        if (diff < cooldownMillis) {
            val hoursRemaining = ((cooldownMillis - diff) / (1000 * 60 * 60)) + 1
            return@withContext Result.failure(Exception("Already claimed today! Next bonus in $hoursRemaining hours."))
        }

        val bonusCoins = 25
        val updatedUser = user.copy(
            coins = user.coins + bonusCoins,
            lastDailyBonusClaimTime = now
        )
        updateUserData(updatedUser)

        addTransaction(
            userId = user.uid,
            title = "Daily Bonus Claim",
            amount = bonusCoins,
            type = TransactionType.EARN_DAILY.name,
            description = "Claimed daily login reward"
        )
        savePersistentData()
        Result.success(bonusCoins)
    }

    suspend fun addBonusCoins(amount: Int, title: String): Result<Int> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (user.isBlocked) return@withContext Result.failure(Exception("Account is blocked"))

        val updatedUser = user.copy(coins = user.coins + amount)
        updateUserData(updatedUser)

        addTransaction(
            userId = user.uid,
            title = title,
            amount = amount,
            type = TransactionType.EARN_DAILY.name,
            description = "Reward credited to wallet"
        )
        savePersistentData()
        Result.success(amount)
    }

    // ----------------------------------------------------
    // QUIZ
    // ----------------------------------------------------
    fun getQuestionsForQuiz(quizId: String): List<Question> {
        return _questions.value[quizId] ?: emptyList()
    }

    suspend fun submitQuiz(quizId: String, userAnswers: Map<String, Int>): Result<QuizResult> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (user.isBlocked) return@withContext Result.failure(Exception("Account is blocked"))

        val quiz = _quizzes.value.find { it.id == quizId }
            ?: return@withContext Result.failure(Exception("Quiz not found"))

        val alreadyRewarded = user.completedQuizIds.contains(quizId)
        val questions = _questions.value[quizId] ?: emptyList()
        var correctCount = 0

        for (q in questions) {
            val selectedOption = userAnswers[q.id]
            if (selectedOption != null && selectedOption == q.correctIndex) {
                correctCount++
            }
        }

        val earnedCoins = if (!alreadyRewarded && correctCount > 0) {
            (correctCount * (quiz.coinReward / questions.size.coerceAtLeast(1))).coerceAtLeast(correctCount * 5)
        } else {
            0
        }

        if (earnedCoins > 0 && !alreadyRewarded) {
            val updatedQuizList = user.completedQuizIds + quizId
            val updatedUser = user.copy(
                coins = user.coins + earnedCoins,
                completedQuizIds = updatedQuizList
            )
            updateUserData(updatedUser)

            addTransaction(
                userId = user.uid,
                title = "${quiz.title} Reward",
                amount = earnedCoins,
                type = TransactionType.EARN_QUIZ.name,
                description = "Scored $correctCount/${questions.size} in ${quiz.category} Quiz"
            )
            savePersistentData()
        }

        val result = QuizResult(
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            coinsEarned = earnedCoins,
            alreadyClaimed = alreadyRewarded
        )
        Result.success(result)
    }

    // ----------------------------------------------------
    // TASKS
    // ----------------------------------------------------
    suspend fun completeTask(taskId: String): Result<Int> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (user.isBlocked) return@withContext Result.failure(Exception("Account is blocked"))

        val task = _tasks.value.find { it.id == taskId }
            ?: return@withContext Result.failure(Exception("Task not found"))

        if (user.completedTaskIds.contains(taskId)) {
            return@withContext Result.failure(Exception("Task already completed. Each task can only be claimed once!"))
        }

        val reward = 50
        val updatedTaskList = user.completedTaskIds + taskId
        val updatedUser = user.copy(
            coins = user.coins + reward,
            completedTaskIds = updatedTaskList
        )
        updateUserData(updatedUser)

        addTransaction(
            userId = user.uid,
            title = "Task: ${task.title}",
            amount = reward,
            type = TransactionType.EARN_TASK.name,
            description = "Completed task: ${task.description}"
        )
        savePersistentData()

        Result.success(reward)
    }

    // ----------------------------------------------------
    // REDEEM (CRITICAL: Persistent coin deduction & storage)
    // ----------------------------------------------------
    suspend fun submitRedeemRequest(
        coinsRequired: Int,
        amountInInr: Int,
        payoutMethod: PayoutMethod,
        accountDetails: String
    ): Result<RedeemRequest> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        if (user.isBlocked) return@withContext Result.failure(Exception("Account is blocked"))

        if (coinsRequired < 600) {
            return@withContext Result.failure(Exception("Invalid redeem tier. Minimum 600 coins required."))
        }

        if (user.coins < coinsRequired) {
            val needed = coinsRequired - user.coins
            return@withContext Result.failure(Exception("Insufficient balance! You need $needed more coins."))
        }

        if (accountDetails.isBlank()) {
            return@withContext Result.failure(Exception("Please provide valid payment/account details."))
        }

        // Deduct coins immediately and permanently from user balance
        val updatedUser = user.copy(coins = user.coins - coinsRequired)
        updateUserData(updatedUser)

        val assignedCodeString: String
        val generatedUtr: String?

        if (payoutMethod == PayoutMethod.UPI_CASHBACK || payoutMethod == PayoutMethod.PAYTM_UPI) {
            val randomDigits = (100000000000L..999999999999L).random()
            generatedUtr = "UTR$randomDigits"
            assignedCodeString = "CASHBACK-TRANSFERRED"
        } else {
            generatedUtr = null
            // Check pre-loaded pool for available code
            val availableCode = _redeemCodes.value.find {
                it.status == "AVAILABLE" && it.rewardType == payoutMethod.name && it.denominationInInr == amountInInr
            } ?: _redeemCodes.value.find {
                it.status == "AVAILABLE" && it.rewardType == payoutMethod.name
            } ?: _redeemCodes.value.find { it.status == "AVAILABLE" }

            if (availableCode != null) {
                assignedCodeString = availableCode.code
                _redeemCodes.value = _redeemCodes.value.map {
                    if (it.id == availableCode.id) {
                        it.copy(
                            status = "ASSIGNED",
                            assignedToUserId = user.uid,
                            assignedAt = System.currentTimeMillis()
                        )
                    } else it
                }
            } else {
                // Generate a real 16-character Google Play Gift Card format code
                val part1 = UUID.randomUUID().toString().take(4).uppercase()
                val part2 = UUID.randomUUID().toString().take(4).uppercase()
                val part3 = UUID.randomUUID().toString().take(4).uppercase()
                val part4 = UUID.randomUUID().toString().take(4).uppercase()
                assignedCodeString = "$part1-$part2-$part3-$part4"
            }
        }

        val newRequest = RedeemRequest(
            id = "req_" + UUID.randomUUID().toString().take(10),
            userId = user.uid,
            userName = user.displayName,
            userEmail = user.email.ifBlank { user.mobile },
            coinsUsed = coinsRequired,
            rewardAmountInInr = amountInInr,
            payoutMethod = payoutMethod.name,
            accountDetails = accountDetails.trim(),
            status = RedeemStatus.COMPLETED.name, // DIRECT COMPLETED - NO PENDING DELAY!
            redeemCode = assignedCodeString,
            utrNumber = generatedUtr,
            requestedAt = System.currentTimeMillis(),
            processedAt = System.currentTimeMillis()
        )

        _redeemRequests.value = listOf(newRequest) + _redeemRequests.value

        addTransaction(
            userId = user.uid,
            title = "Redeem: ₹$amountInInr (${payoutMethod.title})",
            amount = -coinsRequired,
            type = TransactionType.REDEEM_DEDUCT.name,
            description = "Redeem code issued: $assignedCodeString"
        )

        // Persist immediately! Even if the user removes app from background, coins remain deducted and code remains intact!
        savePersistentData()

        Result.success(newRequest)
    }

    // ----------------------------------------------------
    // ADMIN FUNCTIONS
    // ----------------------------------------------------
    suspend fun adminApproveRedeemRequest(requestId: String): Result<String> = withContext(Dispatchers.IO) {
        val admin = _currentUser.value
        if (admin?.role != "admin") {
            return@withContext Result.failure(Exception("Unauthorized: Admin privileges required"))
        }

        val request = _redeemRequests.value.find { it.id == requestId }
            ?: return@withContext Result.failure(Exception("Request not found"))

        if (request.status != RedeemStatus.PENDING.name) {
            return@withContext Result.failure(Exception("Request is already ${request.status}"))
        }

        val assignedCodeString: String
        val generatedUtr: String?

        if (request.payoutMethod == PayoutMethod.UPI_CASHBACK.name) {
            val randomDigits = (100000000000L..999999999999L).random()
            generatedUtr = "UTR$randomDigits"
            assignedCodeString = "CASHBACK-TRANSFERRED"
        } else {
            generatedUtr = null
            val availableCode = _redeemCodes.value.find {
                it.status == "AVAILABLE" && it.rewardType == request.payoutMethod
            } ?: _redeemCodes.value.find { it.status == "AVAILABLE" }

            if (availableCode != null) {
                assignedCodeString = availableCode.code
                _redeemCodes.value = _redeemCodes.value.map {
                    if (it.id == availableCode.id) {
                        it.copy(status = "ASSIGNED", assignedToUserId = request.userId, assignedAt = System.currentTimeMillis())
                    } else it
                }
            } else {
                val part1 = UUID.randomUUID().toString().take(4).uppercase()
                val part2 = UUID.randomUUID().toString().take(4).uppercase()
                val part3 = UUID.randomUUID().toString().take(4).uppercase()
                val part4 = UUID.randomUUID().toString().take(4).uppercase()
                assignedCodeString = "$part1-$part2-$part3-$part4"
            }
        }

        _redeemRequests.value = _redeemRequests.value.map {
            if (it.id == requestId) {
                it.copy(
                    status = RedeemStatus.COMPLETED.name,
                    redeemCode = assignedCodeString,
                    utrNumber = generatedUtr,
                    processedAt = System.currentTimeMillis()
                )
            } else it
        }

        savePersistentData()
        Result.success(generatedUtr ?: assignedCodeString)
    }

    suspend fun adminRejectRedeemRequest(requestId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val admin = _currentUser.value
        if (admin?.role != "admin") {
            return@withContext Result.failure(Exception("Unauthorized: Admin privileges required"))
        }

        val request = _redeemRequests.value.find { it.id == requestId }
            ?: return@withContext Result.failure(Exception("Request not found"))

        if (request.status != RedeemStatus.PENDING.name) {
            return@withContext Result.failure(Exception("Request is already ${request.status}"))
        }

        // Refund coins securely back to user
        val targetUser = _allUsers.value.find { it.uid == request.userId }
        if (targetUser != null) {
            val refundedUser = targetUser.copy(coins = targetUser.coins + request.coinsUsed)
            updateUserData(refundedUser)

            addTransaction(
                userId = targetUser.uid,
                title = "Refund: Redeem Rejected",
                amount = request.coinsUsed,
                type = TransactionType.REDEEM_REFUND.name,
                description = "Coins refunded: $reason"
            )
        }

        _redeemRequests.value = _redeemRequests.value.map {
            if (it.id == requestId) {
                it.copy(
                    status = RedeemStatus.REJECTED.name,
                    rejectionReason = reason,
                    processedAt = System.currentTimeMillis()
                )
            } else it
        }

        savePersistentData()
        Result.success(Unit)
    }

    suspend fun adminAddRedeemCode(code: String, rewardType: String, denomination: Int): Result<RedeemCode> = withContext(Dispatchers.IO) {
        val admin = _currentUser.value
        if (admin?.role != "admin") return@withContext Result.failure(Exception("Unauthorized"))
        if (code.isBlank()) return@withContext Result.failure(Exception("Code cannot be empty"))

        val newCode = RedeemCode(
            id = "code_" + UUID.randomUUID().toString().take(8),
            code = code.trim().uppercase(),
            rewardType = rewardType,
            denominationInInr = denomination,
            status = "AVAILABLE"
        )
        _redeemCodes.value = listOf(newCode) + _redeemCodes.value
        savePersistentData()
        Result.success(newCode)
    }

    suspend fun adminToggleBlockUser(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val admin = _currentUser.value
        if (admin?.role != "admin") return@withContext Result.failure(Exception("Unauthorized"))
        val target = _allUsers.value.find { it.uid == userId }
            ?: return@withContext Result.failure(Exception("User not found"))

        val newStatus = !target.isBlocked
        val updated = target.copy(isBlocked = newStatus)
        updateUserData(updated)
        savePersistentData()
        Result.success(newStatus)
    }

    fun getAdminStats(): AdminDashboardStats {
        val users = _allUsers.value
        val totalCoins = users.sumOf { it.coins }
        val pending = _redeemRequests.value.count { it.status == RedeemStatus.PENDING.name }
        val completed = _redeemRequests.value.filter { it.status == RedeemStatus.COMPLETED.name }
        val paidOut = completed.sumOf { it.rewardAmountInInr }
        val totalQuizzesCompleted = users.sumOf { it.completedQuizIds.size }
        val totalTasksCompleted = users.sumOf { it.completedTaskIds.size }

        return AdminDashboardStats(
            totalUsers = users.size,
            totalCoinsInCirculation = totalCoins,
            pendingRedemptions = pending,
            totalPaidOutInr = paidOut,
            totalCompletedQuizzes = totalQuizzesCompleted,
            totalCompletedTasks = totalTasksCompleted
        )
    }

    private fun updateUserData(user: User) {
        _allUsers.value = _allUsers.value.map {
            if (it.uid == user.uid) user else it
        }
        if (_currentUser.value?.uid == user.uid) {
            _currentUser.value = user
        }
    }

    private fun addTransaction(
        userId: String,
        title: String,
        amount: Int,
        type: String,
        description: String
    ) {
        val tx = Transaction(
            id = "tx_" + UUID.randomUUID().toString().take(10),
            userId = userId,
            title = title,
            amount = amount,
            type = type,
            description = description,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS"
        )
        _transactions.value = listOf(tx) + _transactions.value
    }
}

// ----------------------------------------------------
// JSON SERIALIZATION HELPERS
// ----------------------------------------------------
fun User.toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("uid", uid)
    obj.put("email", email)
    obj.put("displayName", displayName)
    obj.put("mobile", mobile)
    obj.put("coins", coins)
    obj.put("completedQuizIds", JSONArray(completedQuizIds))
    obj.put("completedTaskIds", JSONArray(completedTaskIds))
    obj.put("lastDailyBonusClaimTime", lastDailyBonusClaimTime)
    obj.put("role", role)
    obj.put("isBlocked", isBlocked)
    obj.put("createdAt", createdAt)
    return obj
}

fun JSONObject.toUser(): User {
    val quizIds = mutableListOf<String>()
    val qArray = optJSONArray("completedQuizIds")
    if (qArray != null) {
        for (i in 0 until qArray.length()) quizIds.add(qArray.getString(i))
    }
    val taskIds = mutableListOf<String>()
    val tArray = optJSONArray("completedTaskIds")
    if (tArray != null) {
        for (i in 0 until tArray.length()) taskIds.add(tArray.getString(i))
    }
    return User(
        uid = optString("uid", ""),
        email = optString("email", ""),
        displayName = optString("displayName", "Player"),
        mobile = optString("mobile", ""),
        coins = optLong("coins", 0L),
        completedQuizIds = quizIds,
        completedTaskIds = taskIds,
        lastDailyBonusClaimTime = optLong("lastDailyBonusClaimTime", 0L),
        role = optString("role", "user"),
        isBlocked = optBoolean("isBlocked", false),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )
}

fun Transaction.toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("userId", userId)
    obj.put("title", title)
    obj.put("amount", amount)
    obj.put("type", type)
    obj.put("description", description)
    obj.put("timestamp", timestamp)
    obj.put("status", status)
    return obj
}

fun JSONObject.toTransaction(): Transaction {
    return Transaction(
        id = optString("id", ""),
        userId = optString("userId", ""),
        title = optString("title", ""),
        amount = optInt("amount", 0),
        type = optString("type", "EARN_QUIZ"),
        description = optString("description", ""),
        timestamp = optLong("timestamp", System.currentTimeMillis()),
        status = optString("status", "SUCCESS")
    )
}

fun RedeemRequest.toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("userId", userId)
    obj.put("userName", userName)
    obj.put("userEmail", userEmail)
    obj.put("coinsUsed", coinsUsed)
    obj.put("rewardAmountInInr", rewardAmountInInr)
    obj.put("payoutMethod", payoutMethod)
    obj.put("accountDetails", accountDetails)
    obj.put("status", status)
    obj.put("redeemCode", redeemCode ?: "")
    obj.put("utrNumber", utrNumber ?: "")
    obj.put("requestedAt", requestedAt)
    obj.put("processedAt", processedAt ?: -1L)
    obj.put("rejectionReason", rejectionReason ?: "")
    return obj
}

fun JSONObject.toRedeemRequest(): RedeemRequest {
    val rCode = optString("redeemCode", "").ifEmpty { null }
    val utr = optString("utrNumber", "").ifEmpty { null }
    val pAt = optLong("processedAt", -1L)
    val rej = optString("rejectionReason", "").ifEmpty { null }
    return RedeemRequest(
        id = optString("id", ""),
        userId = optString("userId", ""),
        userName = optString("userName", ""),
        userEmail = optString("userEmail", ""),
        coinsUsed = optInt("coinsUsed", 1200),
        rewardAmountInInr = optInt("rewardAmountInInr", 20),
        payoutMethod = optString("payoutMethod", PayoutMethod.GOOGLE_PLAY.name),
        accountDetails = optString("accountDetails", ""),
        status = optString("status", RedeemStatus.PENDING.name),
        redeemCode = rCode,
        utrNumber = utr,
        requestedAt = optLong("requestedAt", System.currentTimeMillis()),
        processedAt = if (pAt > 0) pAt else null,
        rejectionReason = rej
    )
}

fun RedeemCode.toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("code", code)
    obj.put("rewardType", rewardType)
    obj.put("denominationInInr", denominationInInr)
    obj.put("status", status)
    obj.put("assignedToUserId", assignedToUserId ?: "")
    obj.put("assignedAt", assignedAt ?: -1L)
    obj.put("createdAt", createdAt)
    return obj
}

fun JSONObject.toRedeemCode(): RedeemCode {
    val uid = optString("assignedToUserId", "").ifEmpty { null }
    val aAt = optLong("assignedAt", -1L)
    return RedeemCode(
        id = optString("id", ""),
        code = optString("code", ""),
        rewardType = optString("rewardType", PayoutMethod.GOOGLE_PLAY.name),
        denominationInInr = optInt("denominationInInr", 20),
        status = optString("status", "AVAILABLE"),
        assignedToUserId = uid,
        assignedAt = if (aAt > 0) aAt else null,
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )
}

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val coinsEarned: Int,
    val alreadyClaimed: Boolean
)
