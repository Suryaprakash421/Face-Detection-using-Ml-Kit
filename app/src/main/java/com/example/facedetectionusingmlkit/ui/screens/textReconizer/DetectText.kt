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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.ui.components.MyDropdownMenu
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.textRecognition.TextRecognizer
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun DetectText(
    prefManager: PrefManager,
    textRecognizer: TextRecognizer,
    myViewModel: MyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedText by remember { mutableStateOf<String?>(null) }

    val textRecognitionOption by remember {
        mutableStateOf(myViewModel.textRecognitionOption)
    }

    var selectedIndex by remember { mutableIntStateOf(prefManager.getTextRecognition()) }

    val defaultRecognizer = textRecognitionOption[selectedIndex]

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val scrollState = rememberScrollState()

    // Safe trigger for image recognition
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            textRecognizer.processImage(uri) { result ->
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
        RecognitionOption(textRecognitionOption, defaultRecognizer) {
            val index = textRecognitionOption.indexOf(it)
            selectedIndex = index
            prefManager.setTextRecognition(index)
        }
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

@Composable
fun RecognitionOption(
    options: List<String>,
    default: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    MyDropdownMenu(
        options = options,
        selected = default,
        enabled = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        onChange(it)
    }
}
