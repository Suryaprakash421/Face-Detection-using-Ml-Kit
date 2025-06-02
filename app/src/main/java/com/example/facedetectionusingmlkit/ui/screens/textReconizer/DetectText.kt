package com.example.facedetectionusingmlkit.ui.screens.textReconizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.domain.model.FaceDetectionResult
import com.example.facedetectionusingmlkit.domain.model.ImageFilterResult
import com.example.facedetectionusingmlkit.route.FaceExtract
import com.example.facedetectionusingmlkit.route.TextExtract
import com.example.facedetectionusingmlkit.route.testTabDestinations
import com.example.facedetectionusingmlkit.ui.components.MyDropdownMenu
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.faceDetection.FaceFilterKeys
import com.example.facedetectionusingmlkit.utils.textRecognition.TextRecognizer
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun TestTab(
    prefManager: PrefManager,
    textRecognizer: TextRecognizer,
    myViewModel: MyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            testTabDestinations.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab.title) }
                )
            }
        }

        when (testTabDestinations[selectedTabIndex]) {
            is TextExtract -> DetectText(prefManager, textRecognizer)
            is FaceExtract -> FaceExtractScreen()
        }
    }
}

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

@Composable
fun FaceExtractScreen(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
            if (uri != null) {
                viewModel.processImage(uri)
            } else {
                // Optionally clear results if user cancels picker
                // viewModel.clearResults()
            }
        }
    )

    val faceResults by viewModel.faceResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                // Clear previous results before picking new image
                selectedImageUri = null // Clears the big preview
                // viewModel.clearResults() // You might want a method in VM to clear _faceResults and _errorMessage
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Text("Pick a Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        selectedImageUri?.let { uri ->
            Text("Selected Image Preview:", style = MaterialTheme.typography.titleSmall)
            Image(
                painter = rememberAsyncImagePainter(uri), // Using Coil as per your original code
                contentDescription = "Selected Image Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Fixed height for preview
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit // Changed to Fit for better preview
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (!isLoading && faceResults.isEmpty() && errorMessage == null && selectedImageUri != null) {
            Text(
                text = "No face details to display for the selected image.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(
                items = faceResults,
                key = { index, resultItem ->
                    // Provide a stable key. Index can be a simple key if items don't reorder often,
                    // or use a unique ID from resultItem.
                    // Example: using FACE_INDEX if it's unique for this processing batch
                    resultItem.result.find { it.first == FaceFilterKeys.FACE_INDEX }?.second
                        ?: index
                    // Or simply: index
                }
            ) { index, resultItem: FaceDetectionResult -> // index is Int, resultItem is FaceDetectionResult
                FaceResultItemView(resultItem = resultItem, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FaceResultItemView(resultItem: FaceDetectionResult, viewModel: MyViewModel = hiltViewModel()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                bitmap = resultItem.faceBitmap.asImageBitmap(),
                contentDescription = "Detected Face",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp
                        )
                    ), // Clip only top corners if card has shape
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(10.dp))

            resultItem.result.forEach { pair ->
                val (key, value) = pair
                val displayTitle = viewModel.getDisplayTitle(key)

                val isBadResult = when (key) {
                    FaceFilterKeys.FACE_SIZE_VALID,
                    FaceFilterKeys.POSE_VALID,
                    FaceFilterKeys.EYES_WELL_SEPARATED,
                    FaceFilterKeys.BRIGHTNESS_VALID -> value.contains("false", ignoreCase = true)

                    FaceFilterKeys.IS_BLURRED -> value.equals(
                        "true",
                        ignoreCase = true
                    ) // "IsBlurred: true" is bad
                    FaceFilterKeys.IS_FACE_CONSIDERED_CLEAR -> value.contains(
                        "false",
                        ignoreCase = true
                    )

                    else -> false // For informational keys like variance, count, index
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isBadResult) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        textAlign = TextAlign.End
                    )
                }
                if (resultItem.result.last() != pair) { // Don't add divider after the last item
                    Divider(modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}
