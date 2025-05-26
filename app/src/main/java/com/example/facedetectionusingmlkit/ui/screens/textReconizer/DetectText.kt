package com.example.facedetectionusingmlkit.ui.screens.textReconizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.textRecognition.TextRecognizer

@Composable
fun DetectText(modifier: Modifier = Modifier) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val recognizer = remember { TextRecognizer(context) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val scrollState = rememberScrollState()

    // Safe trigger for image recognition
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            recognizer.processImage(uri) { result ->
                Logger.i("detectedText", "text: $result")
                recognizedText = result
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                )
                selectedImageUri = null
                recognizedText = null
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Pick a Photo")
        }

        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(3f / 4f), // Use a sensible default like 3:4 or 4:3
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(20.dp))
        recognizedText?.let {
            Text("Extracted: ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(it)
        }

    }
}
