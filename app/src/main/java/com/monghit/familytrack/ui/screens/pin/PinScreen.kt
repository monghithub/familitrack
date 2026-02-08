package com.monghit.familytrack.ui.screens.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.monghit.familytrack.services.BiometricHelper

@Composable
fun PinScreen(
    onAuthenticated: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthenticated()
        }
    }

    LaunchedEffect(uiState.biometricAvailable) {
        if (uiState.biometricAvailable && uiState.mode == PinMode.VERIFY) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            if (BiometricHelper.canUseBiometric(activity)) {
                BiometricHelper.authenticate(
                    activity = activity,
                    onSuccess = { viewModel.onBiometricSuccess() },
                    onError = { }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (uiState.mode) {
                PinMode.CREATE -> "Crear PIN"
                PinMode.CONFIRM -> "Confirmar PIN"
                PinMode.VERIFY -> "Introduce tu PIN"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (uiState.mode) {
                PinMode.CREATE -> "Elige un PIN de 4 dígitos"
                PinMode.CONFIRM -> "Repite el PIN para confirmar"
                PinMode.VERIFY -> "Desbloquea FamilyTrack"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PIN dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < uiState.pin.length)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Numpad
        val digits = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(' ', '0', 'D')
        )

        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                row.forEach { char ->
                    when (char) {
                        ' ' -> {
                            if (uiState.biometricAvailable && uiState.mode == PinMode.VERIFY) {
                                IconButton(
                                    onClick = {
                                        val activity = context as? FragmentActivity ?: return@IconButton
                                        BiometricHelper.authenticate(
                                            activity = activity,
                                            onSuccess = { viewModel.onBiometricSuccess() },
                                            onError = { }
                                        )
                                    },
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Fingerprint,
                                        contentDescription = "Biometría",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(64.dp))
                            }
                        }
                        'D' -> {
                            IconButton(
                                onClick = { viewModel.onDelete() },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Backspace,
                                    contentDescription = "Borrar",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.onDigit(char) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
