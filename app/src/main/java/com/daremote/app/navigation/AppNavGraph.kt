package com.daremote.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daremote.app.feature.connections.AddEditServerScreen
import com.daremote.app.feature.connections.ConnectionListScreen
import com.daremote.app.feature.forwarding.AddEditForwardingScreen
import com.daremote.app.feature.forwarding.ForwardingListScreen
import com.daremote.app.feature.snippets.AddEditSnippetScreen
import com.daremote.app.feature.snippets.SnippetListScreen
import com.daremote.app.feature.terminal.TerminalScreen
import com.daremote.app.feature.monitoring.DashboardScreen
import com.daremote.app.feature.monitoring.ProcessListScreen
import com.daremote.app.feature.filemanager.FileManagerScreen
import com.daremote.app.feature.docker.DockerListScreen
import com.daremote.app.feature.docker.ContainerDetailScreen
import com.daremote.app.feature.alerts.AlertConfigScreen
import com.daremote.app.feature.security.SshKeyManagerScreen
import com.daremote.app.feature.settings.SettingsScreen
import com.daremote.app.feature.proxies.AddEditProxyScreen
import com.daremote.app.feature.proxies.ProxyListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Connections.route,
        Screen.Forwarding.route,
        Screen.Snippets.route,
        Screen.Proxies.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Connections.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Bottom nav destinations
            composable(Screen.Connections.route) {
                ConnectionListScreen(
                    onNavigateToAddServer = { navController.navigate(Screen.AddEditServer.createRoute()) },
                    onNavigateToEditServer = { id -> navController.navigate(Screen.AddEditServer.createRoute(id)) },
                    onNavigateToTerminal = { id -> navController.navigate(Screen.Terminal.createRoute(id)) },
                    onNavigateToMonitoring = { id -> navController.navigate(Screen.Monitoring.createRoute(id)) },
                    onNavigateToFileManager = { id -> navController.navigate(Screen.FileManager.createRoute(id)) },
                    onNavigateToDocker = { id -> navController.navigate(Screen.Docker.createRoute(id)) }
                )
            }

            composable(Screen.Forwarding.route) {
                ForwardingListScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddEditForwarding.createRoute()) },
                    onNavigateToEdit = { id -> navController.navigate(Screen.AddEditForwarding.createRoute(id)) }
                )
            }

            composable(Screen.Snippets.route) {
                SnippetListScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddEditSnippet.createRoute()) },
                    onNavigateToEdit = { id -> navController.navigate(Screen.AddEditSnippet.createRoute(id)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAlerts = { navController.navigate(Screen.AlertConfig.route) },
                    onNavigateToKeys = { navController.navigate(Screen.SshKeyManager.route) },
                    onNavigateToProxies = { navController.navigate(Screen.Proxies.route) },
                    onNavigateToBiometric = { navController.navigate(Screen.BiometricSetup.route) }
                )
            }

            // Proxies
            composable(Screen.Proxies.route) {
                ProxyListScreen(
                    onAddProxy = { navController.navigate(Screen.AddEditProxy.createRoute()) },
                    onEditProxy = { id -> navController.navigate(Screen.AddEditProxy.createRoute(id)) }
                )
            }

            composable(
                route = Screen.AddEditProxy.route,
                arguments = listOf(navArgument("proxyId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) {
                AddEditProxyScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            // Add/Edit Server
            composable(
                route = Screen.AddEditServer.route,
                arguments = listOf(navArgument("serverId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                AddEditServerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddProxy = { navController.navigate(Screen.AddEditProxy.createRoute()) }
                )
            }

            // Terminal
            composable(
                route = Screen.Terminal.route,
                arguments = listOf(navArgument("serverId") { type = NavType.LongType })
            ) {
                TerminalScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Monitoring
            composable(
                route = Screen.Monitoring.route,
                arguments = listOf(navArgument("serverId") { type = NavType.LongType })
            ) {
                DashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProcesses = { id -> navController.navigate(Screen.ProcessList.createRoute(id)) }
                )
            }

            composable(
                route = Screen.ProcessList.route,
                arguments = listOf(navArgument("serverId") { type = NavType.LongType })
            ) {
                ProcessListScreen(onNavigateBack = { navController.popBackStack() })
            }

            // File Manager
            composable(
                route = Screen.FileManager.route,
                arguments = listOf(navArgument("serverId") { type = NavType.LongType })
            ) {
                FileManagerScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Docker
            composable(
                route = Screen.Docker.route,
                arguments = listOf(navArgument("serverId") { type = NavType.LongType })
            ) {
                DockerListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToContainer = { serverId, containerId ->
                        navController.navigate(Screen.ContainerDetail.createRoute(serverId, containerId))
                    }
                )
            }

            composable(
                route = Screen.ContainerDetail.route,
                arguments = listOf(
                    navArgument("serverId") { type = NavType.LongType },
                    navArgument("containerId") { type = NavType.StringType }
                )
            ) {
                ContainerDetailScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Forwarding edit
            composable(
                route = Screen.AddEditForwarding.route,
                arguments = listOf(navArgument("ruleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                AddEditForwardingScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Snippet edit
            composable(
                route = Screen.AddEditSnippet.route,
                arguments = listOf(navArgument("snippetId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                AddEditSnippetScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Alerts
            composable(Screen.AlertConfig.route) {
                AlertConfigScreen(onNavigateBack = { navController.popBackStack() })
            }

            // SSH Key Manager
            composable(Screen.SshKeyManager.route) {
                SshKeyManagerScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
