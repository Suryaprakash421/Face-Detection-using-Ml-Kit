package com.example.facedetectionusingmlkit.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.ui.components.HeadingText
import com.example.facedetectionusingmlkit.ui.components.MyDropdownMenu
import com.example.facedetectionusingmlkit.ui.components.MySlider
import com.example.facedetectionusingmlkit.ui.components.MySwitch
import com.example.facedetectionusingmlkit.utils.BitmapCreationMethod
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.utils.FaceDetectionMethods
import com.example.facedetectionusingmlkit.utils.FormatHelper.formatTime
import com.example.facedetectionusingmlkit.utils.HeicDecoderUtil
import com.example.facedetectionusingmlkit.utils.formatToDecimalPlaces
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun Settings(prefManager: PrefManager, myViewModel: MyViewModel = hiltViewModel()) {
    var isAiEnabled by remember { mutableStateOf(prefManager.getAiEnabledState()) }
    var maxThreshHold by remember { mutableStateOf(prefManager.getMaxThreshold()) }
    var minThreshHold by remember { mutableStateOf(prefManager.getMinThreshold()) }
    var minFaceSize by remember { mutableStateOf(prefManager.getMinFaceSize()) }
    var facePadding by remember { mutableStateOf(prefManager.getFacePadding()) }
    var isEditable by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val faceDetectionMode by remember {
        mutableStateOf(myViewModel.faceDetectionMode)
    }
    var isFaceDetectionModeAccurate by remember {
        mutableStateOf(prefManager.isFaceDetectionModeAccurate())
    }
    val defaultMode =
        if (isFaceDetectionModeAccurate) faceDetectionMode[1] else faceDetectionMode[0]

    val bitmapCreationOptions by remember {
        mutableStateOf(myViewModel.bitmapCreationOption)
    }
    var isUseHeicDecoder by remember {
        mutableStateOf(prefManager.isHeicDecoder())
    }
    val currentBitmapOption =
        if (isUseHeicDecoder) bitmapCreationOptions[1] else bitmapCreationOptions[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(scrollState),
    ) {
        SettingAiToggle(
            title = "Enable face detection",
            isChecked = isAiEnabled
        ) { isChecked ->
            isAiEnabled = isChecked
            if (isChecked) {
                isEditable = false
            }
            stopOrStartWorker(myViewModel, isAiEnabled)
            prefManager.setAiEnabledState(isChecked)
        }
        HorizontalDivider(color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        ChangeFaceDetectionMode(
            "Choose Face detection mode",
            enabled = !isAiEnabled,
            options = faceDetectionMode,
            default = defaultMode,
        ) {
            isFaceDetectionModeAccurate = it == FaceDetectionMethods.ACCURATE.name
            prefManager.setIsFaceDetectionModeAccurate(isFaceDetectionModeAccurate)
        }
        ChangeFaceDetectionMode(
            "Choose Bitmap creation option",
            enabled = !isAiEnabled,
            options = bitmapCreationOptions,
            default = currentBitmapOption,
        ) {
            isUseHeicDecoder = it == BitmapCreationMethod.HEIC_DECODER.name
            prefManager.setIsHeicDecoder(isUseHeicDecoder)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Same face threshold
        ThresholdRow(
            title = "Same face threshold",
            threshold = maxThreshHold,
            enabled = !isAiEnabled && isEditable
        ) {
            maxThreshHold = it
            prefManager.setMaxThreshold(it)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Similar face threshold
        ThresholdRow(
            title = "Similar face threshold",
            threshold = minThreshHold,
            enabled = !isAiEnabled && isEditable
        ) {
            minThreshHold = it
            prefManager.setMinThreshold(it)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Min face size
        ThresholdRow(
            title = "Minimum face size",
            threshold = minFaceSize,
            enabled = !isAiEnabled && isEditable
        ) {
            minFaceSize = it
            prefManager.setMinFaceSize(it)
        }

        // Face padding
        ThresholdRow(
            title = "Face padding",
            threshold = facePadding,
            enabled = !isAiEnabled && isEditable
        ) {
            facePadding = it
            prefManager.setFacePadding(it)
        }
        Spacer(modifier = Modifier.height(10.dp))

        EditSetting(isEditable) {
            isEditable = it
            if (it) {
                isAiEnabled = false
                stopOrStartWorker(myViewModel, false)
                prefManager.setAiEnabledState(false)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(color = Color.Gray)

        Spacer(modifier = Modifier.height(10.dp))
        HeadingText(text = "Observations")
        Spacer(modifier = Modifier.height(10.dp))
        Observation(prefManager)
    }
}

@Composable
fun EditSetting(
    isEditable: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Edit")
            MySwitch(isChecked = isEditable, onChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingAiToggle(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        MySwitch(isChecked = isChecked, onChange = onCheckedChange)
    }
}

@Composable
fun ChangeFaceDetectionMode(
    title: String,
    enabled: Boolean,
    default: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        MyDropdownMenu(
            options = options,
            enabled = enabled,
            selected = default,
            modifier = Modifier.fillMaxWidth()
        ) {
            onChange(it)
            Log.i("OnChange", it)
        }
    }
}

@Composable
fun ThresholdRow(
    title: String,
    threshold: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                buildAnnotatedString {
                    append(title) // Normal text
                    append(": ")
                    withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                        append(threshold.formatToDecimalPlaces(2))
                    }
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
        MySlider(
            sliderPosition = threshold,
            valueRange = 0.0f..1.0f,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onValueChange,
            enabled = enabled
        )
    }
}

@Composable
fun Observation(
    prefManager: PrefManager,
    myViewModel: MyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val photoList by myViewModel.galleryImages.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalPhotoCount = photoList.size
    val processedPhotoCount = photoList.filter { it.isProcessed }.size

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TitleAndValue(
                title = "Hardware decode support",
                value = "${HeicDecoderUtil.isHeicHardwareDecodeSupported()}"
            )
            TitleAndValue(title = "Total photos", value = totalPhotoCount.toString())
            TitleAndValue(title = "Processed photos", value = processedPhotoCount.toString())
            TitleAndValue(
                title = "Total time to process $totalPhotoCount photos",
                value = formatTime(prefManager.getProcessedTimes().sum())
            )
//            TitleAndValue(
//                title = "Average time for ${Config.PARALLEL_COUNT} photos",
//                value = formatTime(prefManager.getAverageProcessedTime())
//            )
            TitleAndValue(
                title = "Average time for single photo",
                value = formatTime(prefManager.getAverageProcessedTime() / Config.PARALLEL_COUNT)
            )
            TitleAndValue(
                title = "Average time for Heic photo",
                value = formatTime(prefManager.getAverageHeicImageProcessedTime() / Config.PARALLEL_COUNT)
            )
            TitleAndValue(
                title = "Average time for Other photos",
                value = formatTime(prefManager.getAverageNormalImageProcessedTime() / Config.PARALLEL_COUNT)
            )
            TitleAndValue(
                title = "Maximum memory utilized",
                value = "${prefManager.getMaxMemUsed().formatToDecimalPlaces(2)} MB"
            )
            TitleAndValue(
                title = "Minimum memory utilized",
                value = "${prefManager.getMinMemUsed().formatToDecimalPlaces(2)} MB"
            )
            TitleAndValue(
                title = "Average memory utilized",
                value = "${prefManager.getAvgMemUsed().formatToDecimalPlaces(2)} MB"
            )
        }
    }
}

@Composable
fun TitleAndValue(title: String, value: String, modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            append(title)
            append(": ")
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(value)
            }
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(0.dp, 1.dp)
    )
}

@Composable
fun BitmapCreateOption(
    title: String,
    myViewModel: MyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val options by remember {
        mutableStateOf(myViewModel.bitmapCreationOption)
    }
}

private fun stopOrStartWorker(myViewModel: MyViewModel, isAiEnabled: Boolean) {
    if (isAiEnabled) {
        myViewModel.startFaceDetectionWorker()
    } else {
        myViewModel.stopFaceDetectionWorker()
    }
}


