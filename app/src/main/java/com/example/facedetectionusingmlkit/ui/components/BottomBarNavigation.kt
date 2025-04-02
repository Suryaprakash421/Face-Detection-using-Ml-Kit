package com.example.facedetectionusingmlkit.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.facedetectionusingmlkit.route.Ai
import com.example.facedetectionusingmlkit.route.Home
import com.example.facedetectionusingmlkit.route.Settings

@Composable
fun BottomBarNavigation(navController: NavController, modifier: Modifier = Modifier) {
    val destinationList = listOf(
        Home,
        Ai,
        Settings
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    NavigationBar() {
        destinationList.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {
                    selectedIndex = index
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(text = destination.title) },
                icon = {
                    Icon(imageVector = destination.icon, contentDescription = destination.title)
                })

        }
    }
}