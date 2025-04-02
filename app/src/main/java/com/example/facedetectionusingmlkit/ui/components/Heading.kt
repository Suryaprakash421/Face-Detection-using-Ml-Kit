package com.example.facedetectionusingmlkit.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HeadingText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}