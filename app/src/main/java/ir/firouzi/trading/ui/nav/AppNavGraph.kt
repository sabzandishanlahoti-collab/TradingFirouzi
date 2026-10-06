package ir.firouzi.trading.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.firouzi.trading.ui.chart.ChartScreen
import ir.firouzi.trading.ui.home.HomeScreen
import ir.firouzi.trading.ui.settings.SettingsScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "بازار", Icons.Filled.Storefront),
    Tab("chart", "نمودار", Icons.Filled.ShowChart),
    Tab("settings", "تنظیمات", Icons.Filled.Settings)
)

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = {
            val entry by nav.currentBackStackEntryAsState()
            val current = entry?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = current?.hierarchy?.any {
                        it.route?.startsWith(tab.route) == true
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("chart") { ChartScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
