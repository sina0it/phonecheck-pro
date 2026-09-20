package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAccentColor
import com.example.ui.viewmodel.DiagnosticsViewModel

data class NavItem(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScaffold(
    viewModel: DiagnosticsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val accent = LocalAccentColor.current

    val navItems = listOf(
        NavItem(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(R.string.nav_device, Icons.Filled.PhoneAndroid, Icons.Outlined.PhoneAndroid),
        NavItem(R.string.nav_tests, Icons.Filled.Build, Icons.Outlined.Build),
        NavItem(R.string.nav_health, Icons.Filled.BatteryChargingFull, Icons.Outlined.BatteryChargingFull),
        NavItem(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Luxury Floating Glassmorphic Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val glassColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)
                val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(glassColor)
                        .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedTab = index
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleRes),
                                    tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToDevice = { selectedTab = 1 },
                        onNavigateToHealth = { selectedTab = 3 },
                        onNavigateToTests = { selectedTab = 2 }
                    )
                    1 -> DeviceScreen(viewModel = viewModel)
                    2 -> TestsScreen(viewModel = viewModel)
                    3 -> HealthScreen(viewModel = viewModel)
                    4 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
