package com.example.budgetbruprog7313.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetbruprog7313.data.repository.BudgetRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Colour Palette ───────────────────────────────────────────────────────────
private val DeepSpace      = Color(0xFF07070F)   // page background
private val SurfaceGlass   = Color(0xFF0F0F1E)   // card / field surface
private val BrandIndigo    = Color(0xFF6366F1)   // primary accent
private val BrandEmerald   = Color(0xFF10B981)   // secondary accent
private val BrandPink      = Color(0xFFEC4899)   // tertiary glow
private val OnSurfaceDim   = Color(0x99FFFFFF)   // 60 % white
private val OnSurfaceFaint = Color(0x40FFFFFF)   // 25 % white
private val ErrorRed       = Color(0xFFFC5C7D)

// Gradient helpers
private val brandGradient = Brush.linearGradient(
    colors = listOf(BrandIndigo, BrandEmerald)
)
private val brandGradientHoriz = Brush.horizontalGradient(
    colors = listOf(BrandIndigo, BrandEmerald)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    repository: BudgetRepository
) {
    // ── State ─────────────────────────────────────────────────────────────────
    var username      by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    var isLoading     by remember { mutableStateOf(false) }
    var loginJob      by remember { mutableStateOf<Job?>(null) }

    var usernameFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }

    // Auto-clear error
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) { delay(3500); errorMessage = null }
    }
    DisposableEffect(Unit) { onDispose { loginJob?.cancel() } }

    // ── Animated orb pulse ────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orbPulse"
    )
    val orbShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "orbShift"
    )

    // ── Entry animation ───────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }
    val cardAlpha  by animateFloatAsState(if (visible) 1f else 0f,
        tween(600, easing = FastOutSlowInEasing), label = "cardAlpha")
    val cardOffset by animateFloatAsState(if (visible) 0f else 60f,
        tween(600, easing = FastOutSlowInEasing), label = "cardOffset")

    // ─────────────────────────────────────────────────────────────────────────
    // ROOT: Deep-space background with dynamic orbs
    // ─────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
    ) {
        // Decorative grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGrid(this)
        }

        // Glowing orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            // Indigo orb – top-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrandIndigo.copy(alpha = 0.30f * orbPulse),
                        Color.Transparent
                    ),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * (0.1f + orbShift * 0.08f), h * 0.18f)
            )
            // Emerald orb – bottom-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrandEmerald.copy(alpha = 0.20f * orbPulse),
                        Color.Transparent
                    ),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.85f, h * 0.78f)
            )
            // Pink orb – mid accent
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrandPink.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    radius = w * 0.30f
                ),
                radius = w * 0.30f,
                center = Offset(w * 0.78f, h * 0.35f)
            )
        }

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Brand header ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(brandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = "BudgetBru logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Gradient app name
                    Text(
                        text = "BudgetBru",
                        style = TextStyle(
                            brush = brandGradientHoriz,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "SMART BUDGETING FOR STUDENTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceFaint,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Login Card ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = cardAlpha
                        translationY = cardOffset
                    }
            ) {
                // Outer glow border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    BrandIndigo.copy(alpha = 0.5f),
                                    BrandEmerald.copy(alpha = 0.3f),
                                    BrandPink.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(1.5.dp)  // border thickness
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(27.dp))
                            .background(SurfaceGlass.copy(alpha = 0.95f))
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        // Heading
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Welcome back 👋",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Sign in to your account",
                                fontSize = 13.sp,
                                color = OnSurfaceDim
                            )
                        }

                        // ── Username field ────────────────────────────────────
                        GlassTextField(
                            value = username,
                            onValueChange = { username = it; errorMessage = null },
                            label = "Username",
                            leadingIcon = Icons.Outlined.Person,
                            isFocused = usernameFocused,
                            onFocusChange = { usernameFocused = it },
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { passwordFocusRequester.requestFocus() }
                            )
                        )

                        // ── Password field ────────────────────────────────────
                        GlassTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = "Password",
                            leadingIcon = Icons.Outlined.Lock,
                            isFocused = passwordFocused,
                            onFocusChange = { passwordFocused = it },
                            enabled = !isLoading,
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            trailingContent = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = OnSurfaceDim,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier.focusRequester(passwordFocusRequester)
                        )

                        // ── Error message ─────────────────────────────────────
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ErrorRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorRed,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // ── Login button ──────────────────────────────────────
                        Button(
                            onClick = {
                                if (isLoading) return@Button
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter your username and password"
                                    return@Button
                                }
                                loginJob?.cancel()
                                isLoading = true
                                errorMessage = null
                                keyboardController?.hide()
                                loginJob = scope.launch {
                                    try {
                                        val user = repository.login(
                                            username.trim(), password.trim()
                                        )
                                        if (user != null) {
                                            onLoginSuccess()
                                        } else {
                                            errorMessage = "Invalid credentials — try test / 1234"
                                            isLoading = false
                                        }
                                    } catch (e: CancellationException) {
                                        isLoading = false; throw e
                                    } catch (e: Exception) {
                                        errorMessage = "Login error: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .drawBehind {
                                    // Glow under the button
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(
                                                BrandIndigo.copy(alpha = 0.45f),
                                                Color.Transparent
                                            ),
                                            radius = size.width * 0.6f
                                        ),
                                        radius = size.width * 0.6f,
                                        center = Offset(size.width / 2, size.height * 1.2f)
                                    )
                                },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (!isLoading) brandGradient
                                        else Brush.linearGradient(
                                            listOf(
                                                BrandIndigo.copy(alpha = 0.5f),
                                                BrandEmerald.copy(alpha = 0.5f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            "Authenticating…",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Login,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "Sign In",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ── Divider ───────────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.08f)
                            )
                            Text(
                                "  demo account  ",
                                fontSize = 11.sp,
                                color = OnSurfaceFaint,
                                letterSpacing = 0.5.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.08f)
                            )
                        }

                        // ── Demo pill ─────────────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .clickable {
                                    username = "test"
                                    password = "1234"
                                    errorMessage = null
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Pulsing green dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BrandEmerald)
                            )
                            Text(
                                "Tap to autofill demo credentials",
                                fontSize = 12.sp,
                                color = OnSurfaceDim,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "test / 1234",
                                fontSize = 12.sp,
                                color = BrandEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Footer
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, 300))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = OnSurfaceFaint,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "Secured · Powered by Room Database",
                            fontSize = 11.sp,
                            color = OnSurfaceFaint
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "© 2024 BudgetBru",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.18f)
                    )
                }
            }
        }
    }
}

// ─── Reusable Glassmorphic Text Field ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) BrandIndigo else Color.White.copy(alpha = 0.10f),
        animationSpec = tween(250),
        label = "fieldBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused)
            BrandIndigo.copy(alpha = 0.08f)
        else
            Color.White.copy(alpha = 0.04f),
        animationSpec = tween(250),
        label = "fieldBg"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }
    ) {
        BasicCustomTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            leadingIcon = leadingIcon,
            isFocused = isFocused,
            onFocusChange = onFocusChange,
            enabled = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingContent = trailingContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    enabled: Boolean,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    trailingContent: (@Composable () -> Unit)?
) {
    val iconTint by animateColorAsState(
        if (isFocused) BrandIndigo else OnSurfaceDim,
        tween(250), label = "iconTint"
    )
    val labelColor by animateColorAsState(
        if (isFocused) BrandIndigo else OnSurfaceFaint,
        tween(250), label = "labelColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            leadingIcon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    label,
                    fontSize = 13.sp,
                    color = labelColor,
                    fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { onFocusChange(it.isFocused) },
            enabled = enabled,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White.copy(alpha = 0.85f),
                cursorColor             = BrandIndigo,
                focusedLabelColor       = BrandIndigo,
                unfocusedLabelColor     = OnSurfaceFaint
            ),
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        )
        trailingContent?.invoke()
    }
}

// ─── Canvas utility: subtle dot grid ─────────────────────────────────────────
private fun drawGrid(scope: DrawScope) {
    val spacing = 28.dp.value * scope.density
    val dotColor = Color(0xFF6366F1).copy(alpha = 0.05f)
    var x = 0f
    while (x < scope.size.width) {
        var y = 0f
        while (y < scope.size.height) {
            scope.drawCircle(color = dotColor, radius = 1.2f, center = Offset(x, y))
            y += spacing
        }
        x += spacing
    }
}