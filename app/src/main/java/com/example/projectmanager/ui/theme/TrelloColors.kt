package com.example.projectmanager.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Custom color palette inspired by Trello's design system.
 * These colors are used throughout the app to create a consistent, modern look.
 */
object TrelloColors {
    // Primary Trello brand colors
    val TrelloBlue = Color(0xFF0079BF)
    val TrelloGreen = Color(0xFF70B500)
    val TrelloPurple = Color(0xFF8E44AD)
    val TrelloOrange = Color(0xFFD29034)
    val TrelloRed = Color(0xFFEB5A46)
    
    // Task priority colors
    val LowPriority = Color(0xFF61BD4F)
    val MediumPriority = Color(0xFFFFAB4A)
    val HighPriority = Color(0xFFEB5A46)
    
    // Task status colors
    val TodoColor = Color(0xFF0079BF)
    val InProgressColor = Color(0xFFFFAB4A)
    val ReviewColor = Color(0xFF8E44AD)
    val CompletedColor = Color(0xFF61BD4F)
    val BlockedColor = Color(0xFFEB5A46)
    
    // Board colors for projects
    val BoardColors = listOf(
        "#0079BF", // Blue
        "#70B500", // Green
        "#FF9F1A", // Orange
        "#EB5A46", // Red
        "#F2D600", // Yellow
        "#C377E0", // Purple
        "#00C2E0", // Cyan
        "#51E898", // Mint
        "#FF78CB", // Pink
        "#344563"  // Dark Blue
    )
}
