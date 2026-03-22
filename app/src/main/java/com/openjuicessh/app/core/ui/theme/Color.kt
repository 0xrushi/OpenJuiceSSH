package com.openjuicessh.app.core.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val TerminalGreen = Color(0xFFE0E0E0)
val TerminalBackground = Color(0xFF00303F)
val TerminalToolbarBackground = Color(0xFF004455)
val TerminalButtonText = Color(0xFFE0E0E0)

val StatusConnected = Color(0xFF4CAF50)
val StatusDisconnected = Color(0xFF9E9E9E)
val StatusConnecting = Color(0xFFFFC107)
val StatusError = Color(0xFFF44336)

data class TerminalColorTheme(
    val name: String,
    val background: Color,
    val toolbar: Color,
    val foreground: Color,
    val cursor: Color = foreground
)

val terminalThemes = listOf(
    TerminalColorTheme("Default",       Color(0xFF00303F), Color(0xFF004455), Color(0xFFE0E0E0)),
    TerminalColorTheme("Dracula",       Color(0xFF282A36), Color(0xFF44475A), Color(0xFFF8F8F2), Color(0xFFFF79C6)),
    TerminalColorTheme("Nord",          Color(0xFF2E3440), Color(0xFF3B4252), Color(0xFFD8DEE9)),
    TerminalColorTheme("Solarized Dark",Color(0xFF002B36), Color(0xFF073642), Color(0xFF839496)),
    TerminalColorTheme("Monokai",       Color(0xFF272822), Color(0xFF3E3D32), Color(0xFFF8F8F2)),
    TerminalColorTheme("One Dark",      Color(0xFF282C34), Color(0xFF21252B), Color(0xFFABB2BF)),
)
