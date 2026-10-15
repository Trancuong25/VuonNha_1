package com.example.vuonnha.ui.nav

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.vuonnha.R
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
import com.example.vuonnha.ui.settings.AccountScreen
import com.example.vuonnha.data.AccountSession

private data class Dest(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector)

private val DESTS = listOf(
    Dest("plants", "Vườn Nhà", Icons.Outlined.Home, Icons.Filled.Home),
    Dest("calendar", "Chăm sóc", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    Dest("assistant", "Trợ lý", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
    Dest("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Filled.Settings),
)

@Composable fun AppNav() {
    val account by AccountSession.user.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    fun openTab(route: String) {
        if (current == route) return
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = false }
            launchSingleTop = true
            restoreState = false
        }
    }
    val ctx = LocalContext.current
    val scanHidden by ScanSettings.hiddenState.collectAsStateWithLifecycle()
    val scanVisible = !scanHidden
    LaunchedEffect(Unit) { ScanSettings.hidden(ctx) }
    val rvm: RemindersViewModel = viewModel()
    val tasks by rvm.tasks.collectAsStateWithLifecycle()
    val due = tasks.count { it.daysLeft() <= 0 }

    Scaffold(bottomBar = { if (current != "scan" && current != "account") {
        Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding().selectableGroup().padding(horizontal = 8.dp)) {
            DESTS.forEach { d ->
                val selected = current == d.route ||
                    (d.route == "plants" && current?.startsWith("item/") == true) ||
                    (d.route == "plants" && (current == "reminders" || current == "weather")) ||
                    (d.route == "calendar" && (current == "journal" || current?.startsWith("journal/") == true))
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(Modifier.weight(1f).selectable(selected = selected, role = Role.Tab, onClick = { openTab(d.route) })
                    .padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.width(48.dp).height(3.dp).background(
                        if (selected) tint else Color.Transparent, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)))
                    Spacer(Modifier.height(3.dp))
                        BadgedBox(badge = { if (d.route == "plants" && due > 0) Badge { Text("$due") } }) {
                            when (d.route) {
                                "plants" -> Icon(painterResource(R.drawable.nav_garden), null, Modifier.size(28.dp), tint = tint)
                                "calendar" -> Icon(painterResource(R.drawable.nav_care), null, Modifier.size(28.dp), tint = tint)
                                "assistant" -> Icon(painterResource(R.drawable.nav_assistant), null, Modifier.size(28.dp), tint = tint)
                                else -> Icon(Icons.Outlined.Settings, null, Modifier.size(28.dp), tint = tint)
                            }
                        }
                    Text(d.label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = tint)
                }
            }
        }
        }
    } }) { pad ->
        NavHost(nav, startDestination = "plants", modifier = Modifier.padding(pad)) {
            composable("reminders") { RemindersScreen(onCalendar = { openTab("calendar") }) }
            composable("plants") { PlantsScreen(onOpen = { nav.navigate("item/$it") },
                onCalendar = { openTab("calendar") }, onWeather = { nav.navigate("weather") },
                onReminders = { nav.navigate("reminders") }, onScan = { nav.navigate("scan") }, onAccount = { nav.navigate("account") }) }
            composable("calendar") { CareCalendarScreen({ openTab("plants") }, { nav.navigate("item/$it") }, onJournal = { nav.navigate("journal") }) }
            composable("assistant") { androidx.compose.runtime.key(account?.id) { AssistantScreen(vm = viewModel(key = "assistant-${account?.id ?: "guest"}")) } }
            composable("item/{itemId}") { backStack ->
                val id = backStack.arguments?.getString("itemId")?.toLongOrNull()
                if (id != null) ItemDetailScreen(id, { nav.popBackStack() }, { nav.navigate("journal/$id") })
            }
            composable("journal/{itemId}") { backStack ->
                JournalScreen(initialItemId = backStack.arguments?.getString("itemId")?.toLongOrNull())
            }
            composable("weather") { WeatherScreen(onBack = { nav.popBackStack() }) }
            composable("journal") { JournalScreen() }
            composable("settings") { SettingsScreen(onAccount = { nav.navigate("account") }) }
            composable("account") { AccountScreen(onBack = { nav.popBackStack() }) }
            composable("scan") { PlantScanScreen({ nav.popBackStack() }, { id -> nav.popBackStack(); nav.navigate("item/$id") }) }
        }
    }
}
