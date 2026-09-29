package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EarnTask
import com.example.data.model.PayoutMethod
import com.example.data.model.Quiz
import com.example.data.model.RedeemTier
import com.example.ui.components.AppBackground
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.quiz.QuizListScreen
import com.example.ui.screens.quiz.QuizPlayScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.wallet.RedeemScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.QuizAndEarnTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
            val tasks by viewModel.tasks.collectAsStateWithLifecycle()
            val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
            val userRedeemRequests by viewModel.userRedeemRequests.collectAsStateWithLifecycle()
            val adminStats by viewModel.adminStats.collectAsStateWithLifecycle()
            val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
            val redeemCodes by viewModel.redeemCodes.collectAsStateWithLifecycle()

            // Active Quiz states
            val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
            val activeQuestions by viewModel.activeQuestions.collectAsStateWithLifecycle()
            val currentQIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
            val selectedAnswers by viewModel.selectedAnswers.collectAsStateWithLifecycle()
            val remainingSeconds by viewModel.remainingTimeSeconds.collectAsStateWithLifecycle()
            val quizResult by viewModel.quizResult.collectAsStateWithLifecycle()

            val verifyingTaskId by viewModel.verifyingTaskId.collectAsStateWithLifecycle()

            var currentRoute by remember { mutableStateOf("home") }
            var showAuthModal by remember { mutableStateOf(false) }

            val currentPlayingQuiz = activeQuiz

            val snackbarHostState = remember { SnackbarHostState() }

            // Observe UI messages for Snackbars
            LaunchedEffect(Unit) {
                viewModel.uiMessage.collectLatest { msg ->
                    snackbarHostState.showSnackbar(
                        message = msg,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            // Handle back presses smoothly
            BackHandler(enabled = currentRoute != "home" || currentPlayingQuiz != null) {
                if (currentPlayingQuiz != null) {
                    viewModel.resetQuiz()
                } else if (currentRoute == "redeem") {
                    currentRoute = "wallet"
                } else if (currentRoute == "admin") {
                    currentRoute = "home"
                } else {
                    currentRoute = "home"
                }
            }

            QuizAndEarnTheme(darkTheme = isDarkMode) {
                AppBackground(isDarkMode = isDarkMode) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        topBar = {
                        if (currentPlayingQuiz == null && currentRoute != "admin") {
                            AppTopBar(
                                user = user,
                                language = language,
                                isDarkMode = isDarkMode,
                                onToggleTheme = { viewModel.toggleDarkMode() },
                                onToggleLanguage = {
                                    val next = if (language == AppLanguage.ENGLISH) {
                                        AppLanguage.HINDI
                                    } else {
                                        AppLanguage.ENGLISH
                                    }
                                    viewModel.switchLanguage(next)
                                },
                                onOpenWallet = { currentRoute = "wallet" },
                                onOpenAdmin = { currentRoute = "admin" }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentPlayingQuiz == null && currentRoute != "admin" && currentRoute != "redeem") {
                            AppBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { target -> currentRoute = target },
                                language = language
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Route Switcher
                        when {
                            currentPlayingQuiz != null -> {
                                QuizPlayScreen(
                                    quiz = currentPlayingQuiz,
                                    questions = activeQuestions,
                                    currentIndex = currentQIndex,
                                    selectedAnswers = selectedAnswers,
                                    remainingSeconds = remainingSeconds,
                                    quizResult = quizResult,
                                    language = language,
                                    onSelectOption = { qId, optIdx ->
                                        viewModel.selectOption(qId, optIdx)
                                    },
                                    onNext = { viewModel.nextQuestion() },
                                    onSubmit = { viewModel.submitQuiz() },
                                    onClose = { viewModel.resetQuiz() }
                                )
                            }
                            currentRoute == "admin" -> {
                                AdminDashboardScreen(
                                    stats = adminStats,
                                    users = allUsers,
                                    redeemRequests = userRedeemRequests,
                                    redeemCodes = redeemCodes,
                                    quizzes = quizzes,
                                    tasks = tasks,
                                    language = language,
                                    onApproveRequest = { reqId -> viewModel.adminApproveRequest(reqId) },
                                    onRejectRequest = { reqId, reason -> viewModel.adminRejectRequest(reqId, reason) },
                                    onAddRedeemCode = { code, type, amount -> viewModel.adminAddRedeemCode(code, type, amount) },
                                    onToggleBlockUser = { uId -> viewModel.adminToggleBlockUser(uId) },
                                    onClose = { currentRoute = "home" }
                                )
                            }
                            currentRoute == "home" -> {
                                HomeScreen(
                                    user = user,
                                    quizzes = quizzes,
                                    tasks = tasks,
                                    transactions = userTransactions,
                                    language = language,
                                    onNavigateToQuiz = { selectedQuiz: Quiz? ->
                                        if (selectedQuiz != null) {
                                            viewModel.startQuiz(selectedQuiz)
                                        } else {
                                            currentRoute = "quiz"
                                        }
                                    },
                                    onNavigateToTasks = { currentRoute = "tasks" },
                                    onNavigateToWallet = { currentRoute = "wallet" },
                                    onNavigateToRedeem = { currentRoute = "redeem" },
                                    onClaimDailyBonus = { viewModel.claimDailyBonus() },
                                    onClaimScratchReward = { coins ->
                                        viewModel.claimBonusReward(coins, "Lucky Scratch Card Reward")
                                    }
                                )
                            }
                            currentRoute == "quiz" -> {
                                QuizListScreen(
                                    user = user,
                                    quizzes = quizzes,
                                    language = language,
                                    onStartQuiz = { quiz: Quiz -> viewModel.startQuiz(quiz) }
                                )
                            }
                            currentRoute == "tasks" -> {
                                TasksScreen(
                                    user = user,
                                    tasks = tasks,
                                    verifyingTaskId = verifyingTaskId,
                                    language = language,
                                    onExecuteTask = { task: EarnTask -> viewModel.executeTask(task) }
                                )
                            }
                            currentRoute == "wallet" -> {
                                WalletScreen(
                                    user = user,
                                    transactions = userTransactions,
                                    language = language,
                                    onNavigateToRedeem = { currentRoute = "redeem" }
                                )
                            }
                            currentRoute == "redeem" -> {
                                RedeemScreen(
                                    user = user,
                                    redeemRequests = userRedeemRequests,
                                    language = language,
                                    onSubmitRedeem = { tier: RedeemTier, method: PayoutMethod, account: String ->
                                        viewModel.submitRedeemRequest(tier, method, account) {
                                            // on success
                                        }
                                    },
                                    onBack = { currentRoute = "wallet" }
                                )
                            }
                            currentRoute == "profile" -> {
                                ProfileScreen(
                                    user = user,
                                    language = language,
                                    isDarkMode = isDarkMode,
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onSelectLanguage = { lang: AppLanguage -> viewModel.switchLanguage(lang) },
                                    onOpenAdminDashboard = { currentRoute = "admin" },
                                    onQuickUserLogin = { viewModel.quickLoginDemoUser() },
                                    onQuickAdminLogin = { viewModel.quickLoginDemoAdmin() },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }

                        // Auth Modal Dialog
                        if (showAuthModal) {
                            AuthScreen(
                                language = language,
                                onLogin = { email, pass ->
                                    viewModel.login(email, pass) { showAuthModal = false }
                                },
                                onSignup = { email, pass, name, mobile ->
                                    viewModel.signup(email, pass, name, mobile) { showAuthModal = false }
                                },
                                onQuickUserLogin = {
                                    viewModel.quickLoginDemoUser()
                                    showAuthModal = false
                                },
                                onQuickAdminLogin = {
                                    viewModel.quickLoginDemoAdmin()
                                    showAuthModal = false
                                },
                                onDismiss = { showAuthModal = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
}
