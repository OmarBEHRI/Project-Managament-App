package com.example.projectmanager.ui.project

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.FileAttachment
import com.example.projectmanager.data.model.Project
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectAttachmentsTab(
    project: Project,
    attachments: List<FileAttachment>,
    onUploadAttachment: (String, Long, String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            
            // Get file details
            val contentResolver = context.contentResolver
            val fileName = contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                val mimeType = contentResolver.getType(it) ?: "application/octet-stream"
                
                // Upload the file
                onUploadAttachment(name, size, mimeType, it.toString())
                name
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upload button
        Button(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Attachment")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Attachments list
        Text(
            text = "Attachments (${attachments.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (attachments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No attachments yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments.sortedByDescending { it.uploadedAt }) { attachment ->
                    LocalAttachmentItem(attachment = attachment)
                }
            }
        }
    }
}

@Composable
fun LocalAttachmentItem(attachment: FileAttachment) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File type icon
            val icon = when {
                attachment.mimeType.startsWith("image/") -> Icons.Outlined.Image
                attachment.mimeType.startsWith("video/") -> Icons.Outlined.VideoFile
                attachment.mimeType.startsWith("audio/") -> Icons.Outlined.AudioFile
                attachment.mimeType.contains("pdf") -> Icons.Outlined.PictureAsPdf
                attachment.mimeType.contains("word") -> Icons.Outlined.Description
                attachment.mimeType.contains("excel") || attachment.mimeType.contains("sheet") -> Icons.Outlined.TableChart
                attachment.mimeType.contains("zip") || attachment.mimeType.contains("rar") -> Icons.Outlined.FolderZip
                else -> Icons.Outlined.InsertDriveFile
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // File details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // File size
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Upload date
                    attachment.uploadedAt?.let {
                        Text(
                            text = dateFormatter.format(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Download button
            IconButton(onClick = { /* Download file - will be implemented later */ }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download"
                )
            }
        }
    }
}

// Helper function to format file size
private fun formatFileSize(sizeInBytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    
    return when {
        sizeInBytes < kb -> "$sizeInBytes B"
        sizeInBytes < mb -> String.format("%.1f KB", sizeInBytes.toFloat() / kb)
        sizeInBytes < gb -> String.format("%.1f MB", sizeInBytes.toFloat() / mb)
        else -> String.format("%.1f GB", sizeInBytes.toFloat() / gb)
    }
}
