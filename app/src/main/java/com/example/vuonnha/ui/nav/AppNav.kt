package com.example.vuonnha.ui.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vuonnha.domain.daysLeft
import com.example.vuonnha.ui.journal.JournalScreen
import com.example.vuonnha.ui.plants.PlantsScreen
import com.example.vuonnha.ui.plants.ItemDetailScreen
import com.example.vuonnha.ui.reminders.CareCalendarScreen
import com.example.vuonnha.ui.reminders.RemindersScreen
import com.example.vuonnha.ui.reminders.RemindersViewModel
import com.example.vuonnha.ui.settings.SettingsScreen
import com.example.vuonnha.ui.weather.WeatherScreen
import com.example.vuonnha.ui.plants.PlantScanScreen
import com.example.vuonnha.data.ScanSettings
import com.example.vuonnha.ui.settings.ScanSettingsCard
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChatBubble
import com.example.vuonnha.ui.assistant.AssistantScreen

private data class Dest(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector)

private val DESTS = listOf(
    Dest("plants", "Vườn nhà", Icons.Outlined.Home, Icons.Filled.Home),
    Dest("calendar", "Chăm sóc", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    Dest("assistant", "Trợ lý", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
    Dest("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Filled.Settings),
)

@Composable fun AppNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val ctx = LocalContext.current
    val scanHidden by ScanSettings.hiddenState.collectAsStateWithLifecycle()
    val scanVisible = !scanHidden
    LaunchedEffect(Unit) { ScanSettings.hidden(ctx) }
    val rvm: RemindersViewModel = viewModel()
    val tasks by rvm.tasks.collectAsStateWithLifecycle()
    val due = tasks.count { it.daysLeft() <= 0 }

    Scaffold(bottomBar = { if (current != "scan") {
        Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            DESTS.forEachIndexed { index, d ->
                if (index == 2 && scanVisible) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(onClick = { nav.navigate("scan") { launchSingleTop = true } },
                            modifier = Modifier.size(52.dp), containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary) {
                            Icon(Icons.Outlined.DocumentScanner, "Quét cây bằng máy ảnh")
                        }
                        Text("Quét cây", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
                val selected = current == d.route ||
                    (d.route == "plants" && current?.startsWith("item/") == true) ||
                    (d.route == "plants" && (current == "reminders" || current == "weather")) ||
                    (d.route == "calendar" && (current == "journal" || current?.startsWith("journal/") == true))
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        nav.navigate(d.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        BadgedBox(badge = { if (d.route == "plants" && due > 0) Badge { Text("$due") } }) {
                            Icon(if (selected) d.iconSelected else d.icon, d.label)
                        }
                    },
                    label = { Text(d.label) }
                )
            }
        }
        }
    } }) { pad ->
        NavHost(nav, startDestination = "plants", modifier = Modifier.padding(pad)) {
            composable("reminders") { RemindersScreen(onCalendar = { nav.navigate("calendar") }) }
            composable("plants") { PlantsScreen(onOpen = { nav.navigate("item/$it") },
                onCalendar = { nav.navigate("calendar") }, onWeather = { nav.navigate("weather") },
                onReminders = { nav.navigate("reminders") }, onScan = { nav.navigate("scan") }) }
            composable("calendar") { CareCalendarScreen({ nav.navigate("plants") { popUpTo("plants"); launchSingleTop = true } }, { nav.navigate("item/$it") }, onJournal = { nav.navigate("journal") }) }
            composable("assistant") { AssistantScreen() }
            composable("item/{itemId}") { backStack ->
                val id = backStack.arguments?.getString("itemId")?.toLongOrNull()
                if (id != null) ItemDetailScreen(id, { nav.popBackStack() }, { nav.navigate("journal/$id") })
            }
            composable("journal/{itemId}") { backStack ->
                JournalScreen(initialItemId = backStack.arguments?.getString("itemId")?.toLongOrNull())
            }
            composable("weather") { WeatherScreen(onBack = { nav.popBackStack() }) }
            composable("journal") { JournalScreen() }
            composable("settings") { SettingsScreen() }
            composable("scan") { PlantScanScreen({ nav.popBackStack() }, { id -> nav.popBackStack(); nav.navigate("item/$id") }) }
        }
    }
}
