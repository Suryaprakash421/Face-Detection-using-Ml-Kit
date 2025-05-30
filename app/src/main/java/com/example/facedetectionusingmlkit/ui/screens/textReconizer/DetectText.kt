package com.example.facedetectionusingmlkit.ui.screens.textReconizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.domain.model.ImageFilterResult
import com.example.facedetectionusingmlkit.domain.model.OcrResult
import com.example.facedetectionusingmlkit.ui.components.MyDropdownMenu
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
    var recognizedText by remember { mutableStateOf<ImageFilterResult?>(null) }
//    var recognizedText by remember { mutableStateOf<OcrResult?>(null) }

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
        verticalArrangement = Arrangement.spacedBy(0.dp),
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
                contentScale = ContentScale.None
            )
        }
        Spacer(Modifier.height(10.dp))
        recognizedText?.let {
            Row {
                TitleText("Should process photo: ")
                Text(it.shouldProcessForFaceDetection.toString())
            }
            Spacer(Modifier.height(8.dp))

            Row {
                TitleText("Is human: ")
                Text(it.hasConfidentHuman.toString())
            }
            Spacer(Modifier.height(8.dp))

            Row {
                TitleText("Is cartoon: ")
                Text(it.isStronglyCartoonOrArt.toString())
            }
            Spacer(Modifier.height(8.dp))

            Row {
                TitleText("Is Toy: ")
                Text(it.isLikelyToy.toString())
            }
            Spacer(Modifier.height(8.dp))

            Row {
                TitleText("Is screenshot: ")
                Text(it.isPriorityFilterScreenshot.toString())
            }
            Spacer(Modifier.height(8.dp))

            Row {
                TitleText("Contains text: ")
                Text(it.isPriorityFilterTextHeavy.toString())
            }
            Spacer(Modifier.height(8.dp))

            TitleText("Labels: ")
            Text(it.originalLabelsWithConfidence.toString())
            Spacer(Modifier.height(8.dp))

            TitleText("OCR Extracted: ")
            Text(it.ocrText)
        }
    }
}

@Composable
fun TitleText(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
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
