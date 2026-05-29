package com.example.ui.screens

import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.Translator
import java.util.concurrent.Executors

@Composable
fun LockScreen(
    isBangla: Boolean,
    correctPin: String,
    isBiometricEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputPin by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf(if (isBangla) "সিকিউরিটি পিন লিখুন" else "Enter security PIN") }
    var isError by remember { mutableStateOf(false) }

    // Natural responsive trigger of local biometric scanner if enabled
    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                val biometricPrompt = BiometricPrompt.Builder(context)
                    .setTitle("Income Tracker Secure Entry")
                    .setSubtitle("Confirm Fingerprint or Face to Continue")
                    .setNegativeButton("Cancel PIN bypass", Executors.newSingleThreadExecutor()) { _, _ -> }
                    .build()

                val cancellationSignal = CancellationSignal()
                biometricPrompt.authenticate(
                    cancellationSignal,
                    Executors.newSingleThreadExecutor(),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            onUnlockSuccess()
                        }
                    }
                )
            } catch (e: Exception) {
                // Biometrics scanner is missing or unconfigured in emulator runtime
            }
        }
    }

    // Verify PIN input length reaches exactly 4
    LaunchedEffect(inputPin) {
        if (inputPin.length == 4) {
            if (inputPin == correctPin) {
                isError = false
                onUnlockSuccess()
            } else {
                isError = true
                statusText = if (isBangla) "ভুল পিন, আবার চেষ্টা করুন!" else "Incorrect PIN, try again!"
                inputPin = ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Security Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Secure Lock",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Prompt
        Text(
            text = statusText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Circular indicators showing progress
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val filled = inputPin.length >= i
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Input Grid for PIN entry
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIOMETRIC", "0", "BACKSPACE")
            )

            keyRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { btn ->
                        when (btn) {
                            "BIOMETRIC" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (isBiometricEnabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                                // Trigger biometric prompt on request clickable
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBiometricEnabled) {
                                        Icon(
                                            imageVector = Icons.Filled.Fingerprint,
                                            contentDescription = "Fingerprint",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            "BACKSPACE" -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (inputPin.isNotEmpty()) {
                                                inputPin = inputPin.dropLast(1)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            if (inputPin.length < 4) {
                                                inputPin += btn
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = btn,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
