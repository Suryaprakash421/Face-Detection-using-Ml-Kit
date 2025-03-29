package com.example.facedetectionusingmlkit.screens

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.facedetectionusingmlkit.navigation.FaceDetection
import com.example.facedetectionusingmlkit.navigation.Home

@Composable
fun BottomBarNavigation(navController: NavController, modifier: Modifier = Modifier) {
    val destinationList = listOf(
        Home,
        FaceDetection
    )

    var selectedIndex by remember {
        mutableStateOf(0)
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