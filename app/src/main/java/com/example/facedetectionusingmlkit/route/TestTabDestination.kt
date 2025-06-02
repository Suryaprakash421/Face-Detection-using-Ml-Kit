package com.example.facedetectionusingmlkit.route

interface TestTabDestination {
    val route: String
    val title: String
}

object TextExtract: TestTabDestination {
    override val route: String
        get() = "textExtract"
    override val title: String
        get() = "Text Extract"
}

object FaceExtract: TestTabDestination {
    override val route: String
        get() = "faceExtract"
    override val title: String
        get() = "Face Extract"
}

val testTabDestinations = listOf(TextExtract, FaceExtract)