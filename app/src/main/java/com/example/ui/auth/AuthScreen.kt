package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthScreen(
    authState: AuthState,
    onGoogleSignIn: (String) -> Unit,
    onGuestLogin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var activeAction by remember { mutableStateOf<String?>(null) } // "SIGN_IN", "LOG_IN", "GUEST"
    var localError by remember { mutableStateOf<String?>(null) }

    val isLoading = authState is AuthState.Loading || activeAction != null
    val displayError = localError ?: (authState as? AuthState.Error)?.message

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030412))
    ) {
        AuthLiquidGradientBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_torii),
                contentDescription = "Japanese Dopamine",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(132.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Japanese Dopamine",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 1. Sign In Button (Google Sign-In)
            Button(
                onClick = {
                    localError = null
                    activeAction = "SIGN_IN"
                    launchGoogleCredentialFlow(
                        context = context,
                        credentialManager = credentialManager,
                        preferAuthorizedOnly = false,
                        scope = coroutineScope,
                        onTokenReceived = { idToken ->
                            activeAction = null
                            onGoogleSignIn(idToken)
                        },
                        onCancelled = {
                            activeAction = null
                        },
                        onError = { msg ->
                            activeAction = null
                            localError = msg
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DopamineCrimson,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sign_in_button")
            ) {
                if (activeAction == "SIGN_IN" || (authState is AuthState.Loading && activeAction == null)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Log In Button (Existing Google Account)
            Button(
                onClick = {
                    localError = null
                    activeAction = "LOG_IN"
                    launchGoogleCredentialFlow(
                        context = context,
                        credentialManager = credentialManager,
                        preferAuthorizedOnly = true,
                        scope = coroutineScope,
                        onTokenReceived = { idToken ->
                            activeAction = null
                            onGoogleSignIn(idToken)
                        },
                        onCancelled = {
                            activeAction = null
                        },
                        onError = { msg ->
                            activeAction = null
                            localError = msg
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DopamineCardElevated,
                    contentColor = DopamineTextPrimary
                ),
                border = BorderStroke(1.dp, DopamineOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("log_in_button")
            ) {
                if (activeAction == "LOG_IN") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DopamineTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Guest Button
            OutlinedButton(
                onClick = {
                    localError = null
                    onGuestLogin("Guest Learner")
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = DopamineTextPrimary
                ),
                border = BorderStroke(1.dp, DopamineCrimson.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("guest_enter_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = DopamineCrimson,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Guest",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (!displayError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    color = DopamineCrimson.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, DopamineCrimson.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = displayError,
                        color = DopamineTextPrimary,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun AuthLiquidGradientBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "authLiquidGradient")
    val t1State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )
    val t2State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )

    val cobaltColors = remember {
        listOf(
            Color(0xFF1B2BB8).copy(alpha = 0.72f),
            Color(0xFF0E166E).copy(alpha = 0.45f),
            Color.Transparent
        )
    }
    val edgeColors = remember {
        listOf(
            Color(0xFF6B70FA).copy(alpha = 0.42f),
            Color(0xFF1B2494).copy(alpha = 0.22f),
            Color.Transparent
        )
    }
    val darkWash = remember { Color(0xFF030412).copy(alpha = 0.22f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030412))
    ) {
        val t1 = t1State.value
        val t2 = t2State.value
        val w = size.width
        val h = size.height
        val baseDim = maxOf(w, h)

        val cobaltCenter = Offset(
            x = w * (0.45f + 0.30f * cos(t2)),
            y = h * (0.50f + 0.25f * sin(t1))
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = cobaltColors,
                center = cobaltCenter,
                radius = baseDim * 0.85f
            )
        )

        val ringCenter = Offset(
            x = w * (0.56f + 0.22f * sin(t1)),
            y = h * (0.42f + 0.24f * cos(t2))
        )
        val ringRadius = baseDim * (0.50f + 0.08f * sin(t2))
        drawRect(
            brush = Brush.radialGradient(
                0.00f to Color(0xFF050720).copy(alpha = 0.80f),
                0.28f to Color(0xFF2332BA).copy(alpha = 0.56f),
                0.52f to Color(0xFF7D80FF).copy(alpha = 0.60f),
                0.76f to Color(0xFF2633B5).copy(alpha = 0.34f),
                1.00f to Color.Transparent,
                center = ringCenter,
                radius = ringRadius
            )
        )

        val orbCenter = Offset(
            x = w * (0.32f - 0.24f * cos(t1)),
            y = h * (0.68f - 0.22f * sin(t2))
        )
        drawRect(
            brush = Brush.radialGradient(
                0.00f to Color(0xFF8C8EFF).copy(alpha = 0.52f),
                0.42f to Color(0xFF3E45DC).copy(alpha = 0.40f),
                0.78f to Color(0xFF141E8A).copy(alpha = 0.20f),
                1.00f to Color.Transparent,
                center = orbCenter,
                radius = baseDim * 0.58f
            )
        )

        val edgeCenter = Offset(
            x = w * (0.14f + 0.20f * sin(t2)),
            y = h * (0.28f + 0.28f * cos(t1))
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = edgeColors,
                center = edgeCenter,
                radius = baseDim * 0.50f
            )
        )

        drawRect(color = darkWash)
    }
}

private fun launchGoogleCredentialFlow(
    context: Context,
    credentialManager: CredentialManager,
    preferAuthorizedOnly: Boolean,
    scope: CoroutineScope,
    onTokenReceived: (String) -> Unit,
    onCancelled: () -> Unit,
    onError: (String) -> Unit
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onError("Google Sign-In configuration missing.")
        return
    }

    scope.launch {
        try {
            val result = if (preferAuthorizedOnly) {
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(clientId)
                        .setAutoSelectEnabled(false)
                        .build()
                    val authReq = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    credentialManager.getCredential(context, authReq)
                } catch (_: NoCredentialException) {
                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                    val fallbackReq = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()
                    credentialManager.getCredential(context, fallbackReq)
                }
            } else {
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val req = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()
                credentialManager.getCredential(context, req)
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                onTokenReceived(googleIdToken)
            } else {
                onError("Unexpected credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed: ${e.message}", e)
            onError(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }
}
