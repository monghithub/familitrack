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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monghit.familytrack.BuildConfig
import com.monghit.familytrack.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            HorizontalDivider()
            SettingsSwitchItem(
                title = stringResource(R.string.settings_notifications_sound),
                checked = uiState.soundEnabled,
                onCheckedChange = viewModel::toggleSound
            )
            HorizontalDivider()
            SettingsSwitchItem(
                title = stringResource(R.string.settings_notifications_vibrate),
                checked = uiState.vibrationEnabled,
                onCheckedChange = viewModel::toggleVibration
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Section
        SettingsSection(
            title = stringResource(R.string.settings_account),
            icon = Icons.Default.Person
        ) {
            SettingsTextItem(
                title = stringResource(R.string.settings_user_name),
                value = uiState.userName
            )
            HorizontalDivider()
            SettingsTextItem(
                title = stringResource(R.string.settings_device_name),
                value = uiState.deviceName
            )
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
