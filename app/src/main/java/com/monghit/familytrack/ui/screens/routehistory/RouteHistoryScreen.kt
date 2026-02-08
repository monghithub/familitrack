package com.monghit.familytrack.ui.screens.routehistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.res.stringResource
import com.monghit.familytrack.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: RouteHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))

    val displayDate = try {
        val date = dateFormat.parse(uiState.selectedDate)
        if (date != null) displayFormat.format(date) else uiState.selectedDate
    } catch (_: Exception) {
        uiState.selectedDate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.route_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = dateFormat.parse(uiState.selectedDate) ?: cal.time
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    viewModel.selectDate(dateFormat.format(cal.time))
                }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.route_history_prev_day))
                }
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = dateFormat.parse(uiState.selectedDate) ?: cal.time
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    if (!cal.time.after(Calendar.getInstance().time)) {
                        viewModel.selectDate(dateFormat.format(cal.time))
                    }
                }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.route_history_next_day))
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.route_history_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val points = uiState.points.map { LatLng(it.latitude, it.longitude) }
                val center = points.first()
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(center, 14f)
                }

                Text(
                    text = stringResource(R.string.route_history_points, uiState.points.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = cameraPositionState
                ) {
                    Polyline(
                        points = points,
                        color = androidx.compose.ui.graphics.Color(0xFF1976D2),
                        width = 8f
                    )
                }
            }
        }
    }
}
