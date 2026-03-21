package com.daremote.app.navigation

sealed class Screen(val route: String) {
    // Bottom nav destinations
    object Connections : Screen("connections")
    object Forwarding : Screen("forwarding")
    object Snippets : Screen("snippets")
    object Settings : Screen("settings")

    // Server-scoped screens
    object Terminal : Screen("terminal/{serverId}") {
        fun createRoute(serverId: Long) = "terminal/$serverId"
    }
    object Monitoring : Screen("monitoring/{serverId}") {
        fun createRoute(serverId: Long) = "monitoring/$serverId"
    }
    object ProcessList : Screen("monitoring/{serverId}/processes") {
        fun createRoute(serverId: Long) = "monitoring/$serverId/processes"
    }
    object FileManager : Screen("filemanager/{serverId}") {
        fun createRoute(serverId: Long) = "filemanager/$serverId"
    }
    object FilePreview : Screen("filemanager/{serverId}/preview?path={filePath}") {
        fun createRoute(serverId: Long, filePath: String) =
            "filemanager/$serverId/preview?path=$filePath"
    }
    object Docker : Screen("docker/{serverId}") {
        fun createRoute(serverId: Long) = "docker/$serverId"
    }
    object ContainerDetail : Screen("docker/{serverId}/container/{containerId}") {
        fun createRoute(serverId: Long, containerId: String) =
            "docker/$serverId/container/$containerId"
    }

    // Edit screens
    object AddEditServer : Screen("server/edit?id={serverId}") {
        fun createRoute(serverId: Long? = null) =
            if (serverId != null) "server/edit?id=$serverId" else "server/edit"
    }
    object ServerGroups : Screen("server/groups")
    object AddEditForwarding : Screen("forwarding/edit?id={ruleId}") {
        fun createRoute(ruleId: Long? = null) =
            if (ruleId != null) "forwarding/edit?id=$ruleId" else "forwarding/edit"
    }
    object AddEditSnippet : Screen("snippets/edit?id={snippetId}") {
        fun createRoute(snippetId: Long? = null) =
            if (snippetId != null) "snippets/edit?id=$snippetId" else "snippets/edit"
    }

    // Alerts
    object AlertConfig : Screen("alerts/config")
    object AlertHistory : Screen("alerts/history")

    // Security
    object BiometricSetup : Screen("security/biometric")
    object SshKeyManager : Screen("security/keys")
}
