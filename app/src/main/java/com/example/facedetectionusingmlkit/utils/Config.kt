package com.example.facedetectionusingmlkit.utils

object Config {
    val PARALLEL_COUNT = Runtime.getRuntime().availableProcessors() - 2
    const val FACES_FOLDER = "faces"
}