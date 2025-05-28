package com.example.facedetectionusingmlkit.route

interface WhatsAppTabDestination {
    val route: String
    val title: String
}

object WhatsAppImages : WhatsAppTabDestination {
    override val route: String
        get() = "whatsAppImages"
    override val title: String
        get() = "All"
}

object FilteredWhatsAppImages : WhatsAppTabDestination {
    override val route: String
    get() = "filteredWhatsAppImages"
    override val title: String
    get() = "Filtered"
}

val whatsAppTabDestinations = listOf(WhatsAppImages, FilteredWhatsAppImages)
