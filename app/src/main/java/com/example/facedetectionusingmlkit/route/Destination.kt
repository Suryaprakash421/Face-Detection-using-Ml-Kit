package com.example.facedetectionusingmlkit.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

interface Destination {
    val route: String
    val icon: ImageVector
    val title: String
}

object Home : Destination {
    override val route: String
        get() = "Home"
    override val icon: ImageVector
        get() = Icons.Default.Home
    override val title: String
        get() = "Home"
}

object Ai : Destination {
    override val route: String
        get() = "Ai"
    override val icon: ImageVector
        get() = Icons.Default.Face
    override val title: String
        get() = "AI"
}

object Playground : Destination {
    override val route: String
        get() = "Playground"
    override val icon: ImageVector
        get() = Icons.Default.PlayArrow
    override val title: String
        get() = "Playground"
}

object WhatsApp : Destination {
    override val route: String
        get() = "WhatsApp"
    override val icon: ImageVector
        get() = Icons.Default.Share
    override val title: String
        get() = "WhatsApp"
}

object Settings : Destination {
    override val route: String
        get() = "Settings"
    override val icon: ImageVector
        get() = Icons.Default.Settings
    override val title: String
        get() = "Settings"
}