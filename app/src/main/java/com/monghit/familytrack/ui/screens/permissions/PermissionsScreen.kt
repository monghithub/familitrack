package com.monghit.familytrack.ui.screens.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val fineLocation = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocation = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val locationGranted = fineLocation.status.isGranted || coarseLocation.status.isGranted
    val notificationsGranted = notifications?.status?.isGranted ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permisos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "FamilyTrack necesita estos permisos para funcionar correctamente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                icon = Icons.Filled.LocationOn,
                title = "Ubicacion",
                description = "Necesario para compartir tu posicion con tu familia en tiempo real.",
                isGranted = locationGranted,
                onRequest = { fineLocation.launchPermissionRequest() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Filled.Notifications,
                title = "Notificaciones",
                description = "Recibe alertas cuando un familiar sale de una zona segura o envia un SOS.",
                isGranted = notificationsGranted,
                onRequest = { notifications?.launchPermissionRequest() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Filled.MyLocation,
                title = "Ubicacion en segundo plano",
                description = "Permite compartir la ubicacion incluso cuando la app no esta abierta.",
                isGranted = false,
                onRequest = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                buttonText = "Abrir ajustes"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Filled.BatteryAlert,
                title = "Sin restriccion de bateria",
                description = "Evita que el sistema detenga el servicio de ubicacion para ahorrar bateria.",
                isGranted = false,
                onRequest = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                buttonText = "Desactivar restriccion"
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (locationGranted && notificationsGranted) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    buttonText: String = "Conceder"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isGranted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isGranted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onRequest) {
                        Text(buttonText)
                    }
                }
            }
        }
    }
}
