package com.example.fitness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException // ★ 新增此 Exception
import androidx.credentials.exceptions.GetCredentialException
import com.example.fitness.activity.ActivityLogRepository
import com.example.fitness.auth.SessionManager
import com.example.fitness.auth.SessionRepository
import com.example.fitness.billing.SubscriptionManager
import com.example.fitness.coach.CoachAuthRepository
import com.example.fitness.coach.cloud.CloudSyncUseCase
import com.example.fitness.data.FirebaseTrainingRecordRepository
import com.example.fitness.data.TrainingPlanRepository
import com.example.fitness.firebase.AuthRepository
import com.example.fitness.ui.HomeScreen
import com.example.fitness.ui.PaywallScreen
import com.example.fitness.ui.SignInScreen
import com.example.fitness.ui.theme.ColorScheme
import com.example.fitness.ui.theme.FitnessTheme
import com.example.fitness.ui.theme.ThemeManager
import com.example.fitness.ui.theme.ThemeMode
import com.example.fitness.user.UserRoleProfileRepository
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val repository = TrainingPlanRepository()
    private val trainingRecordRepository = FirebaseTrainingRecordRepository()
    private val activityRepo by lazy { ActivityLogRepository(context = applicationContext, useFirebase = true) }
    private val authRepository = AuthRepository()
    private val sessionRepository by lazy { SessionRepository(this.applicationContext) }
    private val sessionManager by lazy { SessionManager(authRepository, sessionRepository) }

    // ★ 請確認這是正確的 RevenueCat Key
    private val revenueCatApiKey = "goog_VamwiUCRZkIZdyEueLrLfMmiPMq"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) { }
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
            .build()
        MobileAds.setRequestConfiguration(configuration)

        SubscriptionManager.initialize(
            context = this,
            apiKey = revenueCatApiKey,
            userId = authRepository.currentUser()?.uid
        )

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager.getInstance(context) }

            val themeMode by themeManager.themeMode.collectAsState(initial = ThemeMode.AUTO)
            val colorScheme by themeManager.colorScheme.collectAsState(initial = ColorScheme.NEON_BLUE)

            FitnessTheme(
                themeMode = themeMode,
                colorScheme = colorScheme,
                dynamicColor = false
            ) {
                val scope = rememberCoroutineScope()
                val appCtx = context.applicationContext

                val credentialManager = remember { CredentialManager.create(context) }
                val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
                var isSignedIn by remember { mutableStateOf(firebaseUser != null) }

                val isPro by SubscriptionManager.isPro.collectAsState()
                var showPaywall by remember { mutableStateOf(false) }

                val cloudSync = remember { CloudSyncUseCase(CoachAuthRepository(appCtx), repository) }

                LaunchedEffect(firebaseUser) {
                    if (firebaseUser != null) {
                        isSignedIn = true
                        sessionManager.touchIfSignedIn()
                        SubscriptionManager.initialize(appCtx, revenueCatApiKey, firebaseUser.uid)
                    }
                }

                val auth = FirebaseAuth.getInstance()
                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        scope.launch {
                            val currentUser = firebaseAuth.currentUser
                            isSignedIn = currentUser != null
                            if (isSignedIn) {
                                sessionManager.touchIfSignedIn()
                            }
                        }
                    }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                LaunchedEffect(isSignedIn) {
                    if (isSignedIn) {
                        sessionManager.touchIfSignedIn()
                        cloudSync.startAutoReconnectSync()
                    } else {
                        cloudSync.stopListening()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!isSignedIn) {
                        SignInScreen(
                            modifier = Modifier.padding(innerPadding),
                            authRepository = authRepository,
                            onSignedIn = {
                                scope.launch {
                                    sessionManager.touchIfSignedIn()
                                    isSignedIn = true
                                    authRepository.currentUser()?.uid?.let { uid ->
                                        SubscriptionManager.initialize(context, revenueCatApiKey, uid)
                                    }
                                }
                            },
                            onGoogleSignIn = { selectedRole ->
                                // ★ 啟動協程來執行 suspend function
                                lifecycleScope.launch {
                                    handleGoogleSignIn(context, credentialManager, auth, selectedRole) {
                                scope.launch {
                                    sessionManager.touchIfSignedIn()
                                    isSignedIn = true
                                }
                                    }
                                }
                            },
                            onPhoneSignIn = {
                                val intent = Intent(context, PhoneSignInActivity::class.java)
                                context.startActivity(intent)
                            }
                        )
                    } else {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            HomeScreen(
                                repository = repository,
                                activityRepository = activityRepo,
                                trainingRecordRepository = trainingRecordRepository,
                                modifier = Modifier.fillMaxSize(),
                                isPro = isPro,
                                onShowPaywall = { showPaywall = true }
                            )

                            if (showPaywall) {
                                PaywallScreen(
                                    onDismiss = { showPaywall = false },
                                    onPurchaseSuccess = {
                                        showPaywall = false
                                        Toast.makeText(context, "Welcome to Pro!", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (authRepository.currentUser() != null) {
                sessionManager.touchIfSignedIn()
            }
        }
    }

    // ★ 優化的 Google 登入邏輯，包含完整的角色檢查和保存
    private suspend fun handleGoogleSignIn(
        context: android.content.Context,
        credentialManager: CredentialManager,
        auth: FirebaseAuth,
        selectedRole: com.example.fitness.coach.UserRole,
        onSuccess: () -> Unit
    ) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()

                if (authResult.user != null) {
                    val user = authResult.user!!
                    val roleRepo = UserRoleProfileRepository()

                    // 檢查是否為新用戶
                    val existingRole = roleRepo.getMyRole()
                    val isNewUser = existingRole == null

                    if (isNewUser) {
                        // 新用戶：保存選擇的角色和暱稱
                        val saveResult = roleRepo.upsertMyRole(
                            role = selectedRole,
                            nickname = user.displayName
                        )

                        if (saveResult.isSuccess) {
                            Log.d("Auth", "New user role saved: $selectedRole")
                        } else {
                            Log.e("Auth", "Failed to save user role", saveResult.exceptionOrNull())
                        }
                    } else {
                        Log.d("Auth", "Existing user, keeping role: $existingRole")
                    }

                    // 初始化訂閱管理
                    SubscriptionManager.initialize(context, revenueCatApiKey, user.uid)
                    SubscriptionManager.checkSubscriptionStatus()

                    // 觸發會話管理
                    sessionManager.touchIfSignedIn()

                    onSuccess()

                    val welcomeMsg = if (isNewUser) {
                        "歡迎加入！您的角色：${if (selectedRole == com.example.fitness.coach.UserRole.COACH) "教練" else "學員"}"
                    } else {
                        "歡迎回來，${user.displayName ?: "用戶"}！"
                    }
                    Toast.makeText(context, welcomeMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Firebase 驗證失敗", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("Auth", "User cancelled Google Sign-In")
            Toast.makeText(context, "取消登入", Toast.LENGTH_SHORT).show()
        } catch (e: GetCredentialException) {
            Log.e("Auth", "Credential error", e)
            Toast.makeText(context, "憑證錯誤：${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In Error", e)
            Toast.makeText(context, "登入錯誤：${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}