package com.example.vuonnha.ui.nav

import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.vuonnha.ui.reminders.RemindersScreen
import com.example.vuonnha.ui.reminders.RemindersViewModel
import com.example.vuonnha.ui.settings.SettingsScreen
import com.example.vuonnha.ui.weather.WeatherScreen

private data class Dest(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector)

private val DESTS = listOf(
    Dest("reminders", "Hôm nay", Icons.Outlined.TaskAlt, Icons.Filled.TaskAlt),
    Dest("plants", "Vườn", Icons.Outlined.Eco, Icons.Filled.Eco),
    Dest("weather", "Thời tiết", Icons.Outlined.WbSunny, Icons.Filled.WbSunny),
    Dest("journal", "Nhật ký", Icons.Outlined.PhotoLibrary, Icons.Filled.PhotoLibrary),
    Dest("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Filled.Settings),
)

@Composable fun AppNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val rvm: RemindersViewModel = viewModel()
    val tasks by rvm.tasks.collectAsStateWithLifecycle()
    val due = tasks.count { it.daysLeft() <= 0 }

    Scaffold(bottomBar = {
        NavigationBar {
            DESTS.forEach { d ->
                val selected = current == d.route
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
                        BadgedBox(badge = { if (d.route == "reminders" && due > 0) Badge { Text("$due") } }) {
                            Icon(if (selected) d.iconSelected else d.icon, d.label)
                        }
                    },
                    label = { Text(d.label) }
                )
            }
        }
    }) { pad ->
        NavHost(nav, startDestination = "reminders", modifier = Modifier.padding(pad)) {
            composable("reminders") { RemindersScreen() }
            composable("plants") { PlantsScreen() }
            composable("weather") { WeatherScreen() }
            composable("journal") { JournalScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
