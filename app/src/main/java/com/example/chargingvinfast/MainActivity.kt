package com.example.chargingvinfast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.chargingvinfast.ui.theme.ChargingVinfastTheme
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel> { MainViewModel.Factory(application) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannels(this)
        requestPostNotificationPermissionIfNeeded()

        setContent {
            ChargingVinfastTheme {
                ChargingScreen(
                    viewModel = viewModel,
                    onExitApp = { exitAppCompletely() }
                )
            }
        }
    }

    private fun requestPostNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun exitAppCompletely() {
        // Stop charging first to cancel all workers and notifications
        viewModel.stopCharging()
        // Finish all activities
        finishAffinity()
        // Exit the process
        exitProcess(0)
    }
}

@Composable
fun ChargingScreen(viewModel: MainViewModel, onExitApp: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0E1628), Color(0xFF0C1220), Color(0xFF0A0F1A)),
                    ),
                )
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Header()

                StatusCard(uiState)

                ActionButtons(
                    isRunning = uiState.isRunning,
                    onStart = { viewModel.startCharging() },
                    onStop = { viewModel.stopCharging() },
                    onExitApp = onExitApp,
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        IconButton(onClick = { /* decorative only */ }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_charging),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = "Charging Guardian",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Text(
            text = "Theo dõi sạc và nhắc nhở định kỳ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun StatusCard(uiState: ChargingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2D)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Trạng thái",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = if (uiState.isRunning) "Đang nhắc sạc" else "Đã dừng",
                color = if (uiState.isRunning) MaterialTheme.colorScheme.secondary else Color(0xFF9AA5B6),
                style = MaterialTheme.typography.titleLarge,
            )
            AnimatedVisibility(visible = uiState.isRunning && uiState.startedAtMillis != null) {
                val elapsedText = formatElapsed(uiState.startedAtMillis!!)
                Text(
                    text = "Đã theo dõi: $elapsedText",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Thông báo mỗi 1.5 giờ và chuông sau 5 giờ nếu bạn chưa dừng.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onExitApp: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(text = "Charging", color = Color.White, fontSize = 18.sp)
        }

        Button(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
            enabled = isRunning,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                disabledContainerColor = Color(0xFF424242)
            ),
        ) {
            Text(text = "Stop", color = Color.White, fontSize = 18.sp)
        }

        OutlinedButton(
            onClick = onExitApp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFFF9800)
            ),
        ) {
            Text(text = "Thoát hoàn toàn", color = Color(0xFFFF9800), fontSize = 16.sp)
        }

        Text(
            text = "Ứng dụng vẫn hoạt động nền để gửi thông báo, phù hợp cho Galaxy S25 Ultra.\nẤn \"Thoát hoàn toàn\" để dừng mọi hoạt động và đóng app.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatElapsed(startMillis: Long): String {
    val elapsed = System.currentTimeMillis() - startMillis
    val hours = elapsed / (1000 * 60 * 60)
    val minutes = (elapsed / (1000 * 60)) % 60
    return String.format("%02d giờ %02d phút", hours, minutes)
}
