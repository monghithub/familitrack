package com.monghit.familytrack.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monghit.familytrack.BuildConfig
import com.monghit.familytrack.R

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Location Section
            SettingsSection(
                title = stringResource(R.string.settings_location),
                icon = Icons.Default.LocationOn
            ) {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_location_enabled),
                    checked = uiState.isLocationEnabled,
                    onCheckedChange = viewModel::toggleLocationEnabled
                )
                HorizontalDivider()
                IntervalSlider(
                    currentMinutes = uiState.intervalMinutes,
                    onIntervalChange = viewModel::updateInterval
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Section
            SettingsSection(
                title = stringResource(R.string.settings_notifications),
                icon = Icons.Default.Notifications
            ) {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_notifications_enabled),
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Section
            SettingsSection(
                title = "Seguridad",
                icon = Icons.Default.Lock
            ) {
                SettingsSwitchItem(
                    title = "Bloqueo con PIN",
                    checked = uiState.isPinSet,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            viewModel.togglePinEnabled(false)
                        }
                        // PIN creation is handled via PinScreen navigation
                    }
                )
                if (uiState.isPinSet) {
                    HorizontalDivider()
                    SettingsSwitchItem(
                        title = "Desbloqueo biométrico",
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = viewModel::toggleBiometric
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Section
            SettingsSection(
                title = stringResource(R.string.settings_account),
                icon = Icons.Default.Person
            ) {
                SettingsTextItem(
                    title = stringResource(R.string.settings_user_name),
                    value = uiState.userName.ifEmpty { "Usuario ${uiState.userId}" }
                )
                HorizontalDivider()
                SettingsTextItem(
                    title = stringResource(R.string.settings_device_name),
                    value = uiState.deviceName
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Editar perfil")
                    }
                    OutlinedButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Chat familiar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device Info Section
            SettingsSection(
                title = stringResource(R.string.settings_device_info),
                icon = Icons.Default.PhoneAndroid
            ) {
                SettingsTextItem(
                    title = stringResource(R.string.settings_device_id, uiState.deviceId),
                    value = ""
                )
                HorizontalDivider()
                SettingsTextItem(
                    title = stringResource(R.string.settings_user_id, uiState.userId),
                    value = ""
                )
                HorizontalDivider()
                SettingsTextItem(
                    title = stringResource(R.string.settings_registered, if (uiState.isRegistered) "S\u00ed" else "No"),
                    value = ""
                )
                HorizontalDivider()
                SettingsTextItem(
                    title = stringResource(R.string.settings_last_update, uiState.lastLocationUpdate),
                    value = ""
                )
                HorizontalDivider()
                SettingsTokenItem(
                    title = stringResource(R.string.settings_token),
                    token = uiState.deviceToken
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions Section
            SettingsSection(
                title = stringResource(R.string.settings_actions),
                icon = Icons.Default.Build
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::forceLocationSend,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_send_location_now),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = viewModel::sendTestNotification,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_test_notification),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(
                title = stringResource(R.string.settings_about),
                icon = Icons.Default.Info
            ) {
                SettingsTextItem(
                    title = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    value = ""
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            TextButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.settings_logout),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun IntervalSlider(
    currentMinutes: Int,
    onIntervalChange: (Int) -> Unit
) {
    var sliderValue by remember(currentMinutes) { mutableFloatStateOf(currentMinutes.toFloat()) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_location_interval),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.settings_interval_value, sliderValue.toInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onIntervalChange(sliderValue.toInt()) },
            valueRange = 1f..60f,
            steps = 58,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsTextItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsTokenItem(
    title: String,
    token: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        if (token.isNotEmpty()) {
            Text(
                text = "${token.take(20)}...${token.takeLast(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
