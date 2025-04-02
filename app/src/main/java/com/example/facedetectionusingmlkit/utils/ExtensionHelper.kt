package com.example.facedetectionusingmlkit.utils

fun Float.formatToDecimalPlaces(decimalPlaces: Int): String {
    return String.format("%.${decimalPlaces}f", this)
}

fun Double.formatToDecimalPlaces(decimalPlaces: Int): String {
    return String.format("%.${decimalPlaces}f", this)
}