package com.example.facedetectionusingmlkit.ui.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@Composable
fun MySwitch(isChecked: Boolean, onChange: (isChecked: Boolean) -> Unit) {
    Switch(
        checked = isChecked,
        onCheckedChange = { onChange(it) }
    )
}