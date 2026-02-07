package com.monghit.familytrack.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.monghit.familytrack.R
import com.monghit.familytrack.domain.model.SafeZone

@Composable
fun MapScreen(
    onNavigateToSafeZones: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.familyLocations.isEmpty() -> {
                Text(
                    text = stringResource(R.string.map_no_locations),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                FamilyMap(
                    locations = uiState.familyLocations,
                    safeZones = uiState.safeZones,
                    currentUserLocation = uiState.currentUserLocation
                )
            }
        }

        FloatingActionButton(
            onClick = onNavigateToSafeZones,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = stringResource(R.string.safe_zones_title)
            )
        }
    }
}

@Composable
private fun FamilyMap(
    locations: List<FamilyLocationMarker>,
    safeZones: List<SafeZone>,
    currentUserLocation: LatLng?
) {
    val defaultLocation = currentUserLocation ?: LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Safe zones as circles
        safeZones.forEach { zone ->
            Circle(
                center = LatLng(zone.centerLat, zone.centerLng),
                radius = zone.radiusMeters.toDouble(),
                strokeColor = Color(0xFF1976D2),
                strokeWidth = 2f,
                fillColor = Color(0x201976D2)
            )
        }

        // Family member markers
        locations.forEach { marker ->
            Marker(
                state = MarkerState(position = marker.position),
                title = marker.name,
                snippet = marker.lastSeenFormatted
            )
        }
    }
}

data class FamilyLocationMarker(
    val userId: Int,
    val name: String,
    val position: LatLng,
    val lastSeenFormatted: String,
    val isCurrentUser: Boolean = false
)
