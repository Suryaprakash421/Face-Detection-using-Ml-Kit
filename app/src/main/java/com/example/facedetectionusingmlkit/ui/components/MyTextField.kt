package com.example.facedetectionusingmlkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(
    text: String,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        label = { Text(text = label) },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        ),
        modifier = modifier
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color.Transparent),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent, // removes background
            unfocusedIndicatorColor = Color.Transparent, // removes underline when not focused
            focusedIndicatorColor = Color.Transparent,   // removes underline when focused
            disabledIndicatorColor = Color.Transparent
        )
    )
}

