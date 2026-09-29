package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.QuizEarnRepository
import com.example.data.repository.QuizResult
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizEarnRepository(application.applicationContext)

    val currentUser = repository.currentUser
    val quizzes = repository.quizzes
    val tasks = repository.tasks
    val allUsers = repository.allUsers
    val redeemCodes = repository.redeemCodes
    val hasSubscribedChannels = repository.hasSubscribedChannels

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Filtered transactions for current user (or all if admin)
    val userTransactions = combine(currentUser, repository.transactions) { user, txList ->
        if (user?.role == "admin") {
            txList
        } else {
            txList.filter { it.userId == user?.uid }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Redeem requests for user (or all if admin)
    val userRedeemRequests = combine(currentUser, repository.redeemRequests) { user, reqList ->
        if (user?.role == "admin") {
            reqList
        } else {
            reqList.filter { it.userId == user?.uid }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin statistics
    private val _adminStats = MutableStateFlow(repository.getAdminStats())
    val adminStats: StateFlow<AdminDashboardStats> = _adminStats.asStateFlow()

    // Active Quiz State
    private val _activeQuiz = MutableStateFlow<Quiz?>(null)
    val activeQuiz: StateFlow<Quiz?> = _activeQuiz.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestions: StateFlow<List<Question>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, Int>> = _selectedAnswers.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow(15)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()

    private var timerJob: Job? = null

    // Task verification state
    private val _verifyingTaskId = MutableStateFlow<String?>(null)
    val verifyingTaskId: StateFlow<String?> = _verifyingTaskId.asStateFlow()

    init {
        // Refresh admin stats on updates
        viewModelScope.launch {
            combine(allUsers, repository.transactions, repository.redeemRequests) { _, _, _ ->
                repository.getAdminStats()
            }.collect { stats ->
                _adminStats.value = stats
            }
        }
    }

    fun switchLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _uiMessage.emit(msg)
        }
    }

    // ----------------------------------------------------
    // AUTHENTICATION
    // ----------------------------------------------------
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithEmail(email, pass)
            result.onSuccess {
                showMessage("Welcome back, ${it.displayName}!")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Login failed")
            }
        }
    }

    fun loginWithMobile(mobile: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithMobile(mobile, pass)
            result.onSuccess {
                showMessage("Welcome, ${it.displayName}! +200 Welcome Coins added.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Mobile Login failed")
            }
        }
    }

    fun signup(email: String, pass: String, name: String, mobile: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.signupWithEmail(email, pass, name, mobile)
            result.onSuccess {
                showMessage("Account created! 200 welcome coins added.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Signup failed")
            }
        }
    }

    fun signupWithMobile(mobile: String, name: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.signupWithMobile(mobile, name, pass)
            result.onSuccess {
                showMessage("Account created! 200 welcome coins added.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Mobile Signup failed")
            }
        }
    }

    fun claimChannelsSubscription() {
        viewModelScope.launch {
            val result = repository.claimChannelsSubscriptionReward()
            result.onSuccess { bonus ->
                if (bonus > 0) {
                    showMessage("Subscribed to both channels! +$bonus Bonus Coins added to wallet.")
                } else {
                    showMessage("Channel subscription verified!")
                }
            }.onFailure {
                showMessage(it.message ?: "Error verifying channels subscription")
            }
        }
    }

    fun quickLoginDemoUser() {
        repository.quickSwitchUser("user")
        showMessage("Logged in as Demo User (Rahul)")
    }

    fun quickLoginDemoAdmin() {
        repository.quickSwitchUser("admin")
        showMessage("Logged in as Master Admin")
    }

    fun logout() {
        repository.logout()
        resetQuiz()
        showMessage("Signed out successfully")
    }

    // ----------------------------------------------------
    // DAILY BONUS & REWARDS
    // ----------------------------------------------------
    fun claimDailyBonus() {
        viewModelScope.launch {
            val result = repository.claimDailyBonus()
            result.onSuccess { coins ->
                showMessage("Claimed daily bonus! +$coins Coins added to wallet.")
            }.onFailure {
                showMessage(it.message ?: "Could not claim daily bonus.")
            }
        }
    }

    fun claimBonusReward(amount: Int, title: String) {
        viewModelScope.launch {
            val result = repository.addBonusCoins(amount, title)
            result.onSuccess { coins ->
                showMessage("+$coins Coins added to your wallet!")
            }.onFailure {
                showMessage(it.message ?: "Failed to add reward coins")
            }
        }
    }

    // ----------------------------------------------------
    // QUIZ PLAY
    // ----------------------------------------------------
    fun startQuiz(quiz: Quiz) {
        _activeQuiz.value = quiz
        val qList = repository.getQuestionsForQuiz(quiz.id)
        _activeQuestions.value = qList
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _quizResult.value = null
        startTimer(quiz.timeLimitSeconds)
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        _remainingTimeSeconds.value = durationSeconds
        timerJob = viewModelScope.launch {
            while (_remainingTimeSeconds.value > 0) {
                delay(1000)
                _remainingTimeSeconds.value -= 1
            }
            // Auto advance when time runs out
            onTimeExpired()
        }
    }

    private fun onTimeExpired() {
        val qList = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex < qList.size - 1) {
            _currentQuestionIndex.value += 1
            startTimer(_activeQuiz.value?.timeLimitSeconds ?: 15)
        } else {
            submitQuiz()
        }
    }

    fun selectOption(questionId: String, optionIndex: Int) {
        val current = _selectedAnswers.value.toMutableMap()
        current[questionId] = optionIndex
        _selectedAnswers.value = current
    }

    fun nextQuestion() {
        val qList = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex < qList.size - 1) {
            _currentQuestionIndex.value += 1
            startTimer(_activeQuiz.value?.timeLimitSeconds ?: 15)
        } else {
            submitQuiz()
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val quiz = _activeQuiz.value ?: return
        viewModelScope.launch {
            val result = repository.submitQuiz(quiz.id, _selectedAnswers.value)
            result.onSuccess { qRes ->
                _quizResult.value = qRes
                if (qRes.alreadyClaimed) {
                    showMessage("Quiz completed! (Reward already claimed previously)")
                } else if (qRes.coinsEarned > 0) {
                    showMessage("Quiz Completed! You earned +${qRes.coinsEarned} Coins!")
                } else {
                    showMessage("Quiz completed! Practice more to earn coins next time.")
                }
            }.onFailure {
                showMessage(it.message ?: "Error submitting quiz")
            }
        }
    }

    fun resetQuiz() {
        timerJob?.cancel()
        _activeQuiz.value = null
        _activeQuestions.value = emptyList()
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _quizResult.value = null
    }

    // ----------------------------------------------------
    // TASKS
    // ----------------------------------------------------
    fun executeTask(task: EarnTask) {
        val user = currentUser.value ?: return
        if (user.completedTaskIds.contains(task.id)) {
            showMessage("Task already completed!")
            return
        }

        viewModelScope.launch {
            _verifyingTaskId.value = task.id
            // Simulate task verification / action delay (e.g. 2.5 seconds verification)
            delay(2500)
            val result = repository.completeTask(task.id)
            _verifyingTaskId.value = null
            result.onSuccess { coins ->
                showMessage("Task verified! +$coins Coins added to your balance.")
            }.onFailure {
                showMessage(it.message ?: "Task verification failed")
            }
        }
    }

    // ----------------------------------------------------
    // REDEEM
    // ----------------------------------------------------
    fun submitRedeemRequest(
        tier: RedeemTier,
        payoutMethod: PayoutMethod,
        accountDetails: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitRedeemRequest(
                coinsRequired = tier.coinsRequired,
                amountInInr = tier.amountInInr,
                payoutMethod = payoutMethod,
                accountDetails = accountDetails
            )
            result.onSuccess {
                showMessage("Redeem request for ₹${tier.amountInInr} submitted! Status: Pending")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Failed to submit redeem request")
            }
        }
    }

    // ----------------------------------------------------
    // ADMIN ACTIONS
    // ----------------------------------------------------
    fun adminApproveRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.adminApproveRedeemRequest(requestId)
            result.onSuccess { code ->
                showMessage("Request Approved! Assigned Code: $code")
            }.onFailure {
                showMessage(it.message ?: "Approval failed")
            }
        }
    }

    fun adminRejectRequest(requestId: String, reason: String = "Policy violation or invalid account") {
        viewModelScope.launch {
            val result = repository.adminRejectRedeemRequest(requestId, reason)
            result.onSuccess {
                showMessage("Request Rejected and Coins Refunded to user.")
            }.onFailure {
                showMessage(it.message ?: "Rejection failed")
            }
        }
    }

    fun adminAddRedeemCode(code: String, rewardType: String, denomination: Int) {
        viewModelScope.launch {
            val result = repository.adminAddRedeemCode(code, rewardType, denomination)
            result.onSuccess {
                showMessage("Code ${it.code} added to available pool!")
            }.onFailure {
                showMessage(it.message ?: "Failed to add code")
            }
        }
    }

    fun adminToggleBlockUser(userId: String) {
        viewModelScope.launch {
            val result = repository.adminToggleBlockUser(userId)
            result.onSuccess { isBlocked ->
                showMessage(if (isBlocked) "User blocked!" else "User unblocked!")
            }.onFailure {
                showMessage(it.message ?: "Action failed")
            }
        }
    }
}
