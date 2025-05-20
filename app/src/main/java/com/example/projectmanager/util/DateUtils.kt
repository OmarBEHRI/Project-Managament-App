package com.example.projectmanager.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Format a date to a readable string
 */
fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Format a timestamp to a readable date string
 */
fun formatDateFromTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    return formatDate(date)
}

/**
 * Format a file size in bytes to a human-readable string
 */
fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = size.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
} 