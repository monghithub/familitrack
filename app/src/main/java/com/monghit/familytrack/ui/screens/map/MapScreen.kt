package com.monghit.familytrack.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.monghit.familytrack.R

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.familyLocations.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.map_no_locations),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        else -> {
            FamilyMap(
                locations = uiState.familyLocations,
                currentUserLocation = uiState.currentUserLocation
            )
        }
    }
}

@Composable
private fun FamilyMap(
    locations: List<FamilyLocationMarker>,
    currentUserLocation: LatLng?
) {
    val defaultLocation = currentUserLocation ?: LatLng(40.4168, -3.7038) // Madrid default
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
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
